package cn.chitanda.kmmage.network

import cn.chitanda.kmmage.util.newBuilder
import cn.chitanda.kmmage.util.toHttpDateOrNull
import cn.chitanda.kmmage.util.toNonNegativeInt
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLProtocol
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/**
 * @author: Chen
 * @createTime: 2022/4/27 17:11
 * @description:
 **/
class CacheStrategy private constructor(
    val networkRequest: HttpRequestBuilder?,
    val cacheResponse: CacheResponse?
) {
    class Factory(
        private val request: HttpRequestData,
        private val cacheResponse: CacheResponse?
    ) {
        lateinit var a : HttpRequestData
        /** The server's time when the cached response was served, if known. */
        private var servedDate: Date? = null
        private var servedDateString: String? = null

        /** The last modified date of the cached response, if known. */
        private var lastModified: Date? = null
        private var lastModifiedString: String? = null

        /**
         * The expiration date of the cached response, if known.
         * If both this field and the max age are set, the max age is preferred.
         */
        private var expires: Date? = null

        private var requestTime = 0L
        private var responseTime = 0L
        private var etag: String? = null

        /** Age of the cached response. */
        private var ageSeconds = -1

        init {
            if (cacheResponse != null) {
                requestTime = cacheResponse.requestTime
                responseTime = cacheResponse.responseTime
                for (entry in cacheResponse.responseHeaders.entries()) {
                    val name = entry.key
                    val value = entry.value.joinToString(", ")
                    when {
                        name.equals("Date", ignoreCase = true) -> {
                            servedDate = value.toHttpDateOrNull()
                            servedDateString = value
                        }
                        name.equals("Expires", ignoreCase = true) -> {
                            expires = value.toHttpDateOrNull()
                        }
                        name.equals("Last-Modified", ignoreCase = true) -> {
                            lastModified = value.toHttpDateOrNull()
                            lastModifiedString = value
                        }
                        name.equals("ETag", ignoreCase = true) -> {
                            etag = value
                        }
                        name.equals("Age", ignoreCase = true) -> {
                            ageSeconds = value.toNonNegativeInt(-1)
                        }
                    }
                }
            }
        }

        fun compute(): CacheStrategy {
            if (cacheResponse == null) {
                return CacheStrategy(request.newBuilder(), null)
            }
            if (request.url.protocol == URLProtocol.HTTPS) {
                return CacheStrategy(request.newBuilder(), null)
            }
            val responseCaching = cacheResponse.cacheControl
            if (!isCacheable(request, cacheResponse)) {
                return CacheStrategy(request.newBuilder(), null)
            }

            val requestCaching = CacheControl.parse(request.headers)
            if (requestCaching.noCache || hasConditions(request)) {
                return CacheStrategy(request.newBuilder(), null)
            }

            val ageMillis = cacheResponseAge()
            var freshMillis = computeFreshnessLifetime()

            if (requestCaching.maxAgeSeconds != -1) {
                freshMillis = min(
                    freshMillis,
                    TimeUnit.SECONDS.toMillis(requestCaching.maxAgeSeconds.toLong())
                )
            }

            var minFreshMillis = 0L
            if (requestCaching.minFreshSeconds != -1) {
                minFreshMillis = TimeUnit.SECONDS.toMillis(requestCaching.minFreshSeconds.toLong())
            }

            var maxStaleMillis = 0L
            if (!responseCaching.mustRevalidate && requestCaching.maxStaleSeconds != -1) {
                maxStaleMillis = TimeUnit.SECONDS.toMillis(requestCaching.maxStaleSeconds.toLong())
            }

            if (!responseCaching.noCache && ageMillis + minFreshMillis < freshMillis + maxStaleMillis) {
                return CacheStrategy(null, cacheResponse)
            }

            // Find a condition to add to the request.
            // If the condition is satisfied, the response body will not be transmitted.
            val conditionName: String
            val conditionValue: String?
            when {
                etag != null -> {
                    conditionName = "If-None-Match"
                    conditionValue = etag
                }
                lastModified != null -> {
                    conditionName = "If-Modified-Since"
                    conditionValue = lastModifiedString
                }
                servedDate != null -> {
                    conditionName = "If-Modified-Since"
                    conditionValue = servedDateString
                }
                // No condition! Make a regular request.
                else -> return CacheStrategy(request.newBuilder(), null)
            }
            val conditionalRequest = HttpRequestBuilder().apply {
                url(request.url)
                headers {
                    request.headers.forEach { key, value ->
                        append(key, value.joinToString())
                    }
                    append(conditionName, conditionValue!!)
                }

            }
            return CacheStrategy(conditionalRequest, cacheResponse)

        }

        /**
         * Returns the number of milliseconds that the response was fresh for,
         * starting from the served date.
         */
        private fun computeFreshnessLifetime(): Long {
            val responseCaching = cacheResponse!!.cacheControl
            if (responseCaching.maxAgeSeconds != -1) {
                return TimeUnit.SECONDS.toMillis(responseCaching.maxAgeSeconds.toLong())
            }

            val expires = expires
            if (expires != null) {
                val servedMillis = servedDate?.time ?: responseTime
                val delta = expires.time - servedMillis
                return if (delta > 0L) delta else 0L
            }

            if (lastModified != null && request.attributes.allKeys.isNotEmpty()) {
                // As recommended by the HTTP RFC and implemented in Firefox, the max age of a
                // document should be defaulted to 10% of the document's age at the time it was
                // served. Default expiration dates aren't used for URIs containing a query.
                val servedMillis = servedDate?.time ?: requestTime
                val delta = servedMillis - lastModified!!.time
                return if (delta > 0L) delta / 10 else 0L
            }

            return 0L
        }

        /**
         * Returns the current age of the response, in milliseconds.
         * The calculation is specified by RFC 7234, 4.2.3 Calculating Age.
         */
        private fun cacheResponseAge(): Long {
            val servedDate = servedDate
            val apparentReceivedAge = if (servedDate != null) {
                max(0, responseTime - servedDate.time)
            } else {
                0
            }

            val receivedAge = if (ageSeconds != -1) {
                max(apparentReceivedAge, TimeUnit.SECONDS.toMillis(ageSeconds.toLong()))
            } else {
                apparentReceivedAge
            }

            val responseDuration = responseTime - requestTime
            val residentDuration = System.currentTimeMillis() - responseTime
            return receivedAge + responseDuration + residentDuration
        }

        /**
         * Returns true if the request contains conditions that save the server from sending a
         * response that the client has locally. When a request is enqueued with its own conditions,
         * the built-in response cache won't be used.
         */
        private fun hasConditions(request: HttpRequestData): Boolean {
            return request.headers["If-Modified-Since"] != null ||
                    request.headers["If-None-Match"] != null
        }
    }

    companion object {
        /** Returns true if the response can be stored to later serve another request. */
        fun isCacheable(request: HttpRequest, response: HttpResponse): Boolean {
            // A 'no-store' directive on request or response prevents the response from being cached.
            return !CacheControl.parse(request.headers).noStore && !CacheControl.parse(response.headers).noStore &&
                    // Vary all responses cannot be cached.
                    response.headers["Vary"] != "*"
        }

        /** Returns true if the response can be stored to later serve another request. */
        fun isCacheable(request: HttpRequestData, response: CacheResponse): Boolean {
            // A 'no-store' directive on request or response prevents the response from being cached.
            return !CacheControl.parse(request.headers).noStore && !response.cacheControl.noStore &&
                    // Vary all responses cannot be cached.
                    response.responseHeaders["Vary"] != "*"
        }


    }
}
package cn.chitanda.kmmage.network

import cn.chitanda.kmmage.util.toNonNegativeInt
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.parseHeaderValue
import java.util.concurrent.TimeUnit

/**
 * @author: Chen
 * @createTime: 2022/4/28 10:07
 * @description: copy from okhttp
 **/

class CacheControl private constructor(
    @get:JvmName("noCache") val noCache: Boolean,

    @get:JvmName("noStore") val noStore: Boolean,

    @get:JvmName("maxAgeSeconds") val maxAgeSeconds: Int,


    @get:JvmName("sMaxAgeSeconds") val sMaxAgeSeconds: Int,

    val isPrivate: Boolean,
    val isPublic: Boolean,

    @get:JvmName("mustRevalidate") val mustRevalidate: Boolean,

    @get:JvmName("maxStaleSeconds") val maxStaleSeconds: Int,

    @get:JvmName("minFreshSeconds") val minFreshSeconds: Int,

    @get:JvmName("onlyIfCached") val onlyIfCached: Boolean,

    @get:JvmName("noTransform") val noTransform: Boolean,

    @get:JvmName("immutable") val immutable: Boolean,

    private var headerValue: String?
) {
    override fun toString(): String {
        var result = headerValue
        if (result == null) {
            result = buildString {
                if (noCache) append("no-cache, ")
                if (noStore) append("no-store, ")
                if (maxAgeSeconds != -1) append("max-age=").append(maxAgeSeconds).append(", ")
                if (sMaxAgeSeconds != -1) append("s-maxage=").append(sMaxAgeSeconds).append(", ")
                if (isPrivate) append("private, ")
                if (isPublic) append("public, ")
                if (mustRevalidate) append("must-revalidate, ")
                if (maxStaleSeconds != -1) append("max-stale=").append(maxStaleSeconds).append(", ")
                if (minFreshSeconds != -1) append("min-fresh=").append(minFreshSeconds).append(", ")
                if (onlyIfCached) append("only-if-cached, ")
                if (noTransform) append("no-transform, ")
                if (immutable) append("immutable, ")
                if (isEmpty()) return ""
                delete(length - 2, length)
            }
            headerValue = result
        }
        return result
    }
    class Builder {
        private var noCache: Boolean = false
        private var noStore: Boolean = false
        private var maxAgeSeconds = -1
        private var maxStaleSeconds = -1
        private var minFreshSeconds = -1
        private var onlyIfCached: Boolean = false
        private var noTransform: Boolean = false
        private var immutable: Boolean = false

        /** Don't accept an unvalidated cached response. */
        fun noCache() = apply {
            this.noCache = true
        }

        /** Don't store the server's response in any cache. */
        fun noStore() = apply {
            this.noStore = true
        }

        /**
         * Sets the maximum age of a cached response. If the cache response's age exceeds [maxAge], it
         * will not be used and a network request will be made.
         *
         * @param maxAge a non-negative integer. This is stored and transmitted with [TimeUnit.SECONDS]
         *     precision; finer precision will be lost.
         */
        fun maxAge(maxAge: Int, timeUnit: TimeUnit) = apply {
            require(maxAge >= 0) { "maxAge < 0: $maxAge" }
            val maxAgeSecondsLong = timeUnit.toSeconds(maxAge.toLong())
            this.maxAgeSeconds = maxAgeSecondsLong.clampToInt()
        }

        /**
         * Accept cached responses that have exceeded their freshness lifetime by up to `maxStale`. If
         * unspecified, stale cache responses will not be used.
         *
         * @param maxStale a non-negative integer. This is stored and transmitted with
         *     [TimeUnit.SECONDS] precision; finer precision will be lost.
         */
        fun maxStale(maxStale: Int, timeUnit: TimeUnit) = apply {
            require(maxStale >= 0) { "maxStale < 0: $maxStale" }
            val maxStaleSecondsLong = timeUnit.toSeconds(maxStale.toLong())
            this.maxStaleSeconds = maxStaleSecondsLong.clampToInt()
        }

        /**
         * Sets the minimum number of seconds that a response will continue to be fresh for. If the
         * response will be stale when [minFresh] have elapsed, the cached response will not be used and
         * a network request will be made.
         *
         * @param minFresh a non-negative integer. This is stored and transmitted with
         *     [TimeUnit.SECONDS] precision; finer precision will be lost.
         */
        fun minFresh(minFresh: Int, timeUnit: TimeUnit) = apply {
            require(minFresh >= 0) { "minFresh < 0: $minFresh" }
            val minFreshSecondsLong = timeUnit.toSeconds(minFresh.toLong())
            this.minFreshSeconds = minFreshSecondsLong.clampToInt()
        }

        /**
         * Only accept the response if it is in the cache. If the response isn't cached, a `504
         * Unsatisfiable Request` response will be returned.
         */
        fun onlyIfCached() = apply {
            this.onlyIfCached = true
        }

        /** Don't accept a transformed response. */
        fun noTransform() = apply {
            this.noTransform = true
        }

        fun immutable() = apply {
            this.immutable = true
        }

        private fun Long.clampToInt(): Int {
            return when {
                this > Integer.MAX_VALUE -> Integer.MAX_VALUE
                else -> toInt()
            }
        }

        fun build(): CacheControl {
            return CacheControl(noCache, noStore, maxAgeSeconds, -1, false, false, false, maxStaleSeconds,
                minFreshSeconds, onlyIfCached, noTransform, immutable, null)
        }
    }

    companion object {

        /**
         * Cache control request directives that require network validation of responses. Note that such
         * requests may be assisted by the cache via conditional GET requests.
         */
        @JvmField
        val FORCE_NETWORK = Builder()
            .noCache()
            .build()

        /**
         * Cache control request directives that uses the cache only, even if the cached response is
         * stale. If the response isn't available in the cache or requires server validation, the call
         * will fail with a `504 Unsatisfiable Request`.
         */
        @JvmField
        val FORCE_CACHE = Builder()
            .onlyIfCached()
            .maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS)
            .build()
        fun parse(headers: Headers): CacheControl {
            var noCache = false
            var noStore = false
            var maxAgeSeconds = -1
            var sMaxAgeSeconds = -1
            var isPrivate = false
            var isPublic = false
            var mustRevalidate = false
            var maxStaleSeconds = -1
            var minFreshSeconds = -1
            var onlyIfCached = false
            var noTransform = false
            var immutable = false

            var headerValue: String? = null
            val cacheControl = headers[HttpHeaders.CacheControl]?.let {
                headerValue = it
                parseHeaderValue(it)
            } ?: emptyList()
            for (item in cacheControl) {
                val value = item.value.split("=")
                val directive = value.first()
                val parameter = value.last()
                when {
                    "no-cache".equals(directive, ignoreCase = true) -> {
                        noCache = true
                    }
                    "no-store".equals(directive, ignoreCase = true) -> {
                        noStore = true
                    }
                    "max-age".equals(directive, ignoreCase = true) -> {
                        maxAgeSeconds = parameter.toNonNegativeInt(-1)
                    }
                    "s-maxage".equals(directive, ignoreCase = true) -> {
                        sMaxAgeSeconds = parameter.toNonNegativeInt(-1)
                    }
                    "private".equals(directive, ignoreCase = true) -> {
                        isPrivate = true
                    }
                    "public".equals(directive, ignoreCase = true) -> {
                        isPublic = true
                    }
                    "must-revalidate".equals(directive, ignoreCase = true) -> {
                        mustRevalidate = true
                    }
                    "max-stale".equals(directive, ignoreCase = true) -> {
                        maxStaleSeconds = parameter.toNonNegativeInt(Integer.MAX_VALUE)
                    }
                    "min-fresh".equals(directive, ignoreCase = true) -> {
                        minFreshSeconds = parameter.toNonNegativeInt(-1)
                    }
                    "only-if-cached".equals(directive, ignoreCase = true) -> {
                        onlyIfCached = true
                    }
                    "no-transform".equals(directive, ignoreCase = true) -> {
                        noTransform = true
                    }
                    "immutable".equals(directive, ignoreCase = true) -> {
                        immutable = true
                    }
                }

            }
            return CacheControl(
                noCache,
                noStore,
                maxAgeSeconds,
                sMaxAgeSeconds,
                isPrivate,
                isPublic,
                mustRevalidate,
                maxStaleSeconds,
                minFreshSeconds,
                onlyIfCached,
                noTransform,
                immutable,
                headerValue
            )
        }
    }

}
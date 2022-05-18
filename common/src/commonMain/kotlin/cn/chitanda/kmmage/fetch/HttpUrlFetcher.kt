package cn.chitanda.kmmage.fetch

import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.decode.DataSource
import cn.chitanda.kmmage.decode.ImageSource
import cn.chitanda.kmmage.disk.DiskCache
import cn.chitanda.kmmage.network.CacheControl
import cn.chitanda.kmmage.network.CacheResponse
import cn.chitanda.kmmage.network.CacheStrategy
import cn.chitanda.kmmage.network.HttpException
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.util.abortQuietly
import cn.chitanda.kmmage.util.closeQuietly
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.takeFrom
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.cancel
import okio.Closeable
import okio.FileSystem
import okio.IOException
import okio.buffer
import okio.source
import java.io.InputStream

/**
 * @author: Chen
 * @createTime: 2022/4/25 15:52
 * @description:
 **/
internal class HttpUrlFetcher(
    private val url: Url,
    private val client: HttpClient,
    private val diskCache: Lazy<DiskCache?>,
    private val options: Options,
    private val respectCacheHeaders: Boolean
) : Fetcher {
    private val diskCacheKey get() = options.diskCacheKey ?: url.toString()
    private val fileSystem get() = diskCache.value!!.fileSystem
    override suspend fun fetch(): FetchResult {
        var snapshot = readFromDiskCache()
        try {
            val cacheStrategy: CacheStrategy
            if (snapshot != null) {
                if (fileSystem.metadata(snapshot.metadata).size == 0L) {
                    return SourceResult(
                        source = snapshot.toImageSource(),
                        mimeType = null,
                        dataSource = DataSource.DISK
                    )
                }
                if (respectCacheHeaders) {
                    cacheStrategy =
                        CacheStrategy.Factory(newRequest(), snapshot.toCacheResponse()).compute()
                    if (cacheStrategy.networkRequest == null && cacheStrategy.cacheResponse != null) {
                        return SourceResult(
                            source = snapshot.toImageSource(),
                            mimeType = cacheStrategy.cacheResponse.contentType?.toString(),
                            dataSource = DataSource.DISK
                        )
                    }
                } else {
                    return SourceResult(
                        source = snapshot.toImageSource(),
                        mimeType = snapshot.toCacheResponse()?.contentType?.toString(),
                        dataSource = DataSource.DISK
                    )
                }
            } else {
                cacheStrategy = CacheStrategy.Factory(newRequest(), null).compute()
            }
            var response = executeNetworkRequest(cacheStrategy.networkRequest!!)
            try {
                snapshot = writeToDiskCache(snapshot, response, cacheStrategy.cacheResponse)
                if (snapshot != null) {
                    return SourceResult(
                        source = snapshot.toImageSource(),
                        mimeType = snapshot.toCacheResponse()?.contentType?.toString(),
                        dataSource = DataSource.NETWORK
                    )
                }
                var responseBody = response.bodyAsChannel()
                // If we failed to read a new snapshot then read the response body if it's not empty.
                if (responseBody.availableForRead > 0) {
                    return SourceResult(
                        source = responseBody.toImageSource(),
                        mimeType = response.headers[HttpHeaders.ContentType],
                        dataSource = responseBody.toDataSource()
                    )
                } else {
                    // If the response body is empty, execute a new network request without the
                    // cache headers.
                    response.cancel()
                    response = executeNetworkRequest(HttpRequestBuilder().takeFrom(newRequest()))
                    responseBody = response.bodyAsChannel()

                    return SourceResult(
                        source = responseBody.toImageSource(),
                        mimeType = response.headers[HttpHeaders.ContentType],
                        dataSource = responseBody.toDataSource()
                    )
                }

            } catch (e: Exception) {
                snapshot?.closeQuietly()
                throw  e
            }
        } catch (e: Exception) {
            snapshot?.closeQuietly()
            throw  e
        }
    }

    private fun ByteReadChannel.toImageSource(): ImageSource {
        return ImageSource(
            toInputStream().source().buffer(),
            FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toFile()
        )
    }

    private fun ByteReadChannel.toDataSource(): DataSource {
        return if (this.availableForRead > 0) DataSource.NETWORK else DataSource.DISK
    }

    private suspend fun executeNetworkRequest(request: HttpRequestBuilder): HttpResponse {
        val response = client.request(request)
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NotModified) {
            response.body<Closeable>().closeQuietly()
            throw HttpException(response)
        }
        return response
    }

    private suspend fun writeToDiskCache(
        snapshot: DiskCache.Snapshot?,
        response: HttpResponse,
        cacheResponse: CacheResponse?
    ): DiskCache.Snapshot? {
        val request = response.request
        if (!isCacheable(request, response)) {
            snapshot?.closeQuietly()
            return null
        }
        val editor = if (snapshot != null) {
            snapshot.closeAndEdit()
        } else {
            diskCache.value?.edit(diskCacheKey)
        }

        editor ?: return null
        try {
            if (response.status == HttpStatusCode.NotModified && cacheResponse != null) {
                fileSystem.write(editor.metadata) {
                    CacheResponse(response).writeTo(this)
                }
            } else {
                // Update the metadata and the image data.
                fileSystem.write(editor.metadata) {
                    CacheResponse(response).writeTo(this)
                }
                fileSystem.write(editor.data) {
                    response.body<InputStream>().source().buffer().readAll(this)
                }
            }
            return editor.commitAndGet()
        } catch (e: Exception) {
            editor.abortQuietly()
            throw e
        } finally {
            response.cancel()
        }
    }

    private fun isCacheable(request: HttpRequest, response: HttpResponse): Boolean {
        return options.diskCachePolicy.writeEnabled &&
                (!respectCacheHeaders || CacheStrategy.isCacheable(request, response))
    }

    private fun readFromDiskCache(): DiskCache.Snapshot? {
        return if (options.diskCachePolicy.readEnabled) {
            diskCache.value?.get(diskCacheKey)
        } else {
            null
        }
    }

    private fun newRequest(): HttpRequestData {
        return HttpRequestBuilder().apply {
            url(this@HttpUrlFetcher.url)
            headers {
                appendAll(options.headers)
            }
            val diskRead = options.diskCachePolicy.readEnabled
            val networkRead = options.networkCachePolicy.readEnabled
            when {
                !networkRead && diskRead -> {
                    header(HttpHeaders.CacheControl, CacheControl.FORCE_CACHE)
                }
                networkRead && !diskRead -> if (options.diskCachePolicy.writeEnabled) {
                    header(HttpHeaders.CacheControl, CacheControl.FORCE_NETWORK)
                } else {
                    header(HttpHeaders.CacheControl, CacheControl.FORCE_NETWORK)
                    header(HttpHeaders.CacheControl, CACHE_CONTROL_FORCE_NETWORK_NO_CACHE)
                }
                !networkRead && !diskRead -> {
                    // This causes the request to fail with a 504 Unsatisfiable Request.
                    header(HttpHeaders.CacheControl, CACHE_CONTROL_NO_NETWORK_NO_CACHE)
                }
            }
        }.build()
    }

    private fun DiskCache.Snapshot.toImageSource(): ImageSource {
        return ImageSource(data, fileSystem, diskCacheKey, this)
    }

    private fun DiskCache.Snapshot.toCacheResponse(): CacheResponse? {
        return try {
            fileSystem.read(metadata) {
                CacheResponse(this)
            }
        } catch (_: IOException) {
            // If we can't parse the metadata, ignore this entry.
            null
        }
    }

    class Factory(
        private val ktorClient: HttpClient,
        private val diskCache: Lazy<DiskCache?>,
        private val respectCacheHeaders: Boolean
    ) : Fetcher.Factory<Url> {
        override fun create(
            data: Url,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            if (!isApplicable(data)) return null
            return HttpUrlFetcher(data, ktorClient, diskCache, options, respectCacheHeaders)
        }

        private fun isApplicable(url: Url): Boolean {
            return url.protocol.name == "http" || url.protocol.name == "https"
        }
    }

    companion object {

        private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().noStore().build()

        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().onlyIfCached().build()

    }
}
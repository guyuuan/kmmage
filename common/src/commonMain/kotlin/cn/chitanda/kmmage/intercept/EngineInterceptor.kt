package cn.chitanda.kmmage.intercept

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.ComponentRegistry
import cn.chitanda.kmmage.EventListener
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.decode.DataSource
import cn.chitanda.kmmage.decode.DecodeResult
import cn.chitanda.kmmage.decode.FileImageSource
import cn.chitanda.kmmage.fetch.FetchResult
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.memory.MemoryCacheService
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.request.RequestService
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.util.closeQuietly
import cn.chitanda.kmmage.util.eventListener
import cn.chitanda.kmmage.util.isPlaceholderCached
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * @author: Chen
 * @createTime: 2022/5/13 10:42
 * @description:
 **/
internal class EngineInterceptor(
    private val imageLoader: ImageLoader,
    private val requestService: RequestService
) : Interceptor {
    private val memoryCacheService = MemoryCacheService(imageLoader, requestService)
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        try {
            val request = chain.request
            val data = request.data
            val size = chain.size
            val eventListener = chain.eventListener
            val options = requestService.options(request, size)
            val scale = options.scale
            eventListener.mapStart(request, data)
            val mappedData = imageLoader.components.map(data, options)
            eventListener.mapEnd(request, mappedData)
            val cacheKey =
                memoryCacheService.newCacheKey(request, mappedData, options, eventListener)
            val cacheValue =
                cacheKey?.let { memoryCacheService.getCacheValue(request, cacheKey, size, scale) }
            if (cacheValue != null) {
                return memoryCacheService.newResult(chain, request, cacheKey, cacheValue)
            }

            return withContext(Dispatchers.IO) {
                val result = execute(request, mappedData, options, eventListener)
                // Write the result to the memory cache.
                val isCached = memoryCacheService.setCacheValue(cacheKey, request, result)

                // Return the result.
                SuccessResult(
                    bitmap = result.bitmap,
                    request = request,
                    dataSource = result.dataSource,
                    memoryCacheKey = cacheKey.takeIf { isCached },
                    diskCacheKey = result.diskCacheKey,
                    isSampled = result.isSampled,
                    isPlaceholderCached = chain.isPlaceholderCached,
                )
            }

        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            } else {
                return requestService.errorResult(chain.request, throwable)
            }
        }
    }

    private suspend fun execute(
        request: ImageRequest,
        data: Any,
        _options: Options,
        eventListener: EventListener
    ): ExecuteResult {
        var options = _options
        val components = imageLoader.components
        var fetchResult: FetchResult? = null
        val executeResult = try {
            if (!requestService.allowHardwareWorkerThread(options)) {
                options = options.copy(config = ImageBitmapConfig.Argb8888)
            }

            fetchResult = fetch(request, components, data, options, eventListener)
            when (fetchResult) {
                is SourceResult -> withContext(Dispatchers.IO) {
                    decode(fetchResult, components, request, data, options, eventListener)
                }
            }
        } finally {
            (fetchResult as? SourceResult)?.source?.closeQuietly()
        }
        // Apply any transformations and prepare to draw.
        val finalResult = transform(executeResult, request, options, eventListener)
        return finalResult
    }

    private suspend fun transform(
        result: ExecuteResult,
        request: ImageRequest,
        options: Options,
        eventListener: EventListener
    ): ExecuteResult {
        val transformations = request.transformations
        if (transformations.isEmpty()) return result

        // Skip the transformations as converting to a bitmap is disabled.
        if (!request.allowConversionToBitmap) {
            return result
        }

        // Apply the transformations.
        return withContext(Dispatchers.IO) {
            val input = result.bitmap
            eventListener.transformStart(request, input)
            val output = transformations.fold(input) { bitmap, transformation ->
                transformation.transform(bitmap, options.size).also { ensureActive() }
            }
            eventListener.transformEnd(request, output)
            result.copy(bitmap = output)
        }
    }

    private suspend fun decode(
        fetchResult: SourceResult,
        components: ComponentRegistry,
        request: ImageRequest,
        data: Any,
        options: Options,
        eventListener: EventListener
    ): ExecuteResult {
        val decodeResult: DecodeResult
        var searchIndex = 0
        while (true) {
            val pair = components.newDecoder(fetchResult, options, imageLoader, searchIndex)
            checkNotNull(pair) { "Unable to create a decoder that supports: $data" }
            val decoder = pair.first
            searchIndex = pair.second + 1

            eventListener.decodeStart(request, decoder, options)
            val result = decoder.decode()
            eventListener.decodeEnd(request, decoder, options, result)

            if (result != null) {
                decodeResult = result
                break
            }
        }

        // Combine the fetch and decode operations' results.
        return ExecuteResult(
            bitmap = decodeResult.bitmap,
            isSampled = decodeResult.isSampled,
            dataSource = fetchResult.dataSource,
            diskCacheKey = (fetchResult.source as? FileImageSource)?.diskCacheKey
        )
    }

    private suspend fun fetch(
        request: ImageRequest,
        components: ComponentRegistry,
        data: Any,
        options: Options,
        eventListener: EventListener
    ): FetchResult {
        val fetchResult: FetchResult
        var searchIndex = 0
        while (true) {
            val pair = components.newFetcher(data, options, imageLoader, searchIndex)
            checkNotNull(pair) { "Unable to create a fetcher that supports: $data" }
            val fetcher = pair.first
            searchIndex = pair.second + 1

            eventListener.fetchStart(request, fetcher, options)
            val result = fetcher.fetch()
            try {
                eventListener.fetchEnd(request, fetcher, options, result)
            } catch (throwable: Throwable) {
                // Ensure the source is closed if an exception occurs before returning the result.
                (result as? SourceResult)?.source?.closeQuietly()
                throw throwable
            }

            if (result != null) {
                fetchResult = result
                break
            }
        }
        return fetchResult
    }

    data class ExecuteResult(
        val bitmap: ImageBitmap,
        val isSampled: Boolean,
        val dataSource: DataSource,
        val diskCacheKey: String?
    )
}
package cn.chitanda.kmmage.memory

import cn.chitanda.kmmage.EventListener
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.decode.DataSource
import cn.chitanda.kmmage.decode.DecodeUtils
import cn.chitanda.kmmage.intercept.EngineInterceptor
import cn.chitanda.kmmage.intercept.Interceptor
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.request.RequestService
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.size.pxOrElse
import cn.chitanda.kmmage.util.allowInexactSize
import cn.chitanda.kmmage.util.isMinOrMax
import cn.chitanda.kmmage.util.isPlaceholderCached
import kotlin.math.abs

/**
 * @author: Chen
 * @createTime: 2022/5/13 11:43
 * @description:
 **/
internal class MemoryCacheService(
    private val imageLoader: ImageLoader,
    private val requestService: RequestService
) {
    fun newCacheKey(
        request: ImageRequest,
        mappedData: Any,
        options: Options,
        eventListener: EventListener
    ): MemoryCache.Key? {
        request.memoryCacheKey?.let { return it }
        eventListener.keyStart(request, mappedData)
        val base = imageLoader.components.key(mappedData, options)
        eventListener.keyEnd(request, base)
        if (base == null) return null
        val transformations = request.transformations
        val parameterKeys = request.parameters.memoryCacheKeys()
        if (transformations.isEmpty() && parameterKeys.isEmpty()) {
            return MemoryCache.Key(base)
        }
        val extras = parameterKeys.toMutableMap()
        if (transformations.isNotEmpty()) {
            request.transformations.forEachIndexed { index, transformation ->
                extras[EXTRA_TRANSFORMATION_INDEX + index] = transformation.cacheKey
            }
            extras[EXTRA_TRANSFORMATION_SIZE] = options.size.toString()
        }
        return MemoryCache.Key(base, extras)
    }

    fun getCacheValue(
        request: ImageRequest,
        cacheKey: MemoryCache.Key,
        size: Size,
        scale: Scale,
    ): MemoryCache.Value? {
        if (!request.memoryCachePolicy.readEnabled) return null
        val cacheValue = imageLoader.memoryCache?.get(cacheKey)
        return cacheValue?.takeIf { isCacheValueValid(request, cacheKey, cacheValue, size, scale) }
    }

    internal fun isCacheValueValid(
        request: ImageRequest,
        cacheKey: MemoryCache.Key,
        cacheValue: MemoryCache.Value,
        size: Size,
        scale: Scale,
    ): Boolean {
        // Ensure we don't return a hardware bitmap if the request doesn't allow it.
        if (!requestService.isConfigValidForHardware(request, cacheValue.bitmap.config)) {
            return false
        }
        // Ensure the size of the cached bitmap is valid for the request.
        return isSizeValid(request, cacheKey, cacheValue, size, scale)
    }

    private fun isSizeValid(
        request: ImageRequest,
        cacheKey: MemoryCache.Key,
        cacheValue: MemoryCache.Value,
        size: Size,
        scale: Scale
    ): Boolean {
        // The cached value must not be sampled if the image's original size is requested.
        val isSampled = cacheValue.isSampled
        if (size.isOriginal) {
            return !isSampled
        }

        // The requested dimensions must match the transformation size exactly if it is present.
        // Unlike standard, requests we can't assume transformed bitmaps for the same image have
        // the same aspect ratio.
        val transformationSize = cacheKey.extras[EXTRA_TRANSFORMATION_SIZE]
        if (transformationSize != null) {
            // 'Size.toString' is safe to use to determine equality.
            return transformationSize == size.toString()
        }

        // Compute the scaling factor between the source dimensions and the requested dimensions.
        val srcWidth = cacheValue.bitmap.width
        val srcHeight = cacheValue.bitmap.height
        val dstWidth = size.width.pxOrElse { Int.MAX_VALUE }
        val dstHeight = size.height.pxOrElse { Int.MAX_VALUE }
        val multiplier = DecodeUtils.computeSizeMultiplier(
            srcWidth = srcWidth,
            srcHeight = srcHeight,
            dstWidth = dstWidth,
            dstHeight = dstHeight,
            scale = scale
        )

        // Short circuit the size check if the size is at most 1 pixel off in either dimension.
        // This accounts for the fact that downsampling can often produce images with dimensions
        // at most one pixel off due to rounding.
        val allowInexactSize = request.allowInexactSize
        if (allowInexactSize) {
            val downsampleMultiplier = multiplier.coerceAtMost(1.0)
            if (abs(dstWidth - (downsampleMultiplier * srcWidth)) <= 1 ||
                abs(dstHeight - (downsampleMultiplier * srcHeight)) <= 1
            ) {
                return true
            }
        } else {
            if ((dstWidth.isMinOrMax() || abs(dstWidth - srcWidth) <= 1) &&
                (dstHeight.isMinOrMax() || abs(dstHeight - srcHeight) <= 1)
            ) {
                return true
            }
        }

        // The cached value must be equal to the requested size if precision == exact.
        if (multiplier != 1.0 && !allowInexactSize) {
            return false
        }

        // The cached value must be larger than the requested size if the cached value is sampled.
        if (multiplier > 1.0 && isSampled) {
            return false
        }

        return true
    }

    fun newResult(
        chain: Interceptor.Chain,
        request: ImageRequest,
        cacheKey: MemoryCache.Key,
        cacheValue: MemoryCache.Value
    ): ImageResult = SuccessResult(
        bitmap = cacheValue.bitmap,
        request = request,
        dataSource = DataSource.MEMORY_CACHE,
        memoryCacheKey = cacheKey,
        diskCacheKey = cacheValue.diskCacheKey,
        isSampled = cacheValue.isSampled,
        isPlaceholderCached = chain.isPlaceholderCached,
    )

    fun setCacheValue(
        cacheKey: MemoryCache.Key?,
        request: ImageRequest,
        result: EngineInterceptor.ExecuteResult
    ): Boolean {
        if (!request.memoryCachePolicy.writeEnabled) return false
        val memoryCache = imageLoader.memoryCache
        if (memoryCache == null || cacheKey == null) return false
        val bitmap = result.bitmap

        // Create and set the memory cache value.
        val extras = mutableMapOf<String, Any>()
        extras[EXTRA_IS_SAMPLED] = result.isSampled
        result.diskCacheKey?.let { extras[EXTRA_DISK_CACHE_KEY] = it }
        memoryCache[cacheKey] = MemoryCache.Value(bitmap, extras)
        return true
    }

    private val MemoryCache.Value.isSampled: Boolean
        get() = (extras[EXTRA_IS_SAMPLED] as? Boolean) ?: false

    private val MemoryCache.Value.diskCacheKey: String?
        get() = extras[EXTRA_DISK_CACHE_KEY] as? String

    companion object {
        internal const val EXTRA_TRANSFORMATION_INDEX = "#transformation_"
        internal const val EXTRA_TRANSFORMATION_SIZE = "#transformation_size"
        internal const val EXTRA_IS_SAMPLED = "#is_sampled"
        internal const val EXTRA_DISK_CACHE_KEY = "#disk_cache_key"
    }
}
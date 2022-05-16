package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.decode.DataSource
import cn.chitanda.kmmage.memory.MemoryCache

/**
 * @author: Chen
 * @createTime: 2022/4/6 15:03
 * @description:
 **/
sealed class ImageResult {
    abstract val bitmap: ImageBitmap?
    abstract val request: ImageRequest
}

class SuccessResult(
    override val bitmap: ImageBitmap,
    override val request: ImageRequest,

    /**
     * the data source that the image was loaded from
     */
    val dataSource: DataSource,

    /**
     * the cache key for the image in the memory cache
     * it is 'null' if the image was not written to the memory cache
     * */
    val memoryCacheKey: MemoryCache.Key? = null,

    /**
     * the cache key for the image in the disk cache
     * it's 'null' if the image wasn't written to the disk cache
     */
    val diskCacheKey: String? = null,

    /**
     * 'true' if the image is sampled (i.e. loaded into memory at less than its original size)
     */
    val isSampled: Boolean = false,

    /**
     * 'true' if [ImageRequest.placeholderMemoryCacheKey] was present in memory cache
     */
    val isPlaceholderCached: Boolean = false
) : ImageResult() {

    fun copy(
        bitmap: ImageBitmap = this.bitmap,
        request: ImageRequest = this.request,
        dataSource: DataSource = this.dataSource,
        memoryCacheKey: MemoryCache.Key? = this.memoryCacheKey,
        diskCacheKey: String? = this.diskCacheKey,
        isSampled: Boolean = this.isSampled,
        isPlaceholderCached: Boolean = this.isPlaceholderCached,
    ) = SuccessResult(
        bitmap = bitmap,
        request = request,
        dataSource = dataSource,
        memoryCacheKey = memoryCacheKey,
        diskCacheKey = diskCacheKey,
        isSampled = isSampled,
        isPlaceholderCached = isPlaceholderCached,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is SuccessResult &&
                bitmap == other.bitmap &&
                request == other.request &&
                dataSource == other.dataSource &&
                memoryCacheKey == other.memoryCacheKey &&
                diskCacheKey == other.diskCacheKey &&
                isSampled == other.isSampled &&
                isPlaceholderCached == other.isPlaceholderCached
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + dataSource.hashCode()
        result = 31 * result + memoryCacheKey.hashCode()
        result = 31 * result + diskCacheKey.hashCode()
        result = 31 * result + isSampled.hashCode()
        result = 31 * result + isPlaceholderCached.hashCode()
        return result
    }
}

class ErrorResult(
    override val bitmap: ImageBitmap? = null,
    override val request: ImageRequest,
    val throwable: Throwable
) : ImageResult() {
    fun copy(
        bitmap: ImageBitmap? = this.bitmap,
        request: ImageRequest = this.request,
        throwable: Throwable = this.throwable
    ) = ErrorResult(bitmap, request, throwable)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ErrorResult
                && this.bitmap == other.bitmap
                && this.request == other.request
                && this.throwable == other.throwable

    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + throwable.hashCode()
        return result
    }
}
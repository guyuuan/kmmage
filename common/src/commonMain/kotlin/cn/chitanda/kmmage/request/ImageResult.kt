package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.decode.DataSource

/**
 * Represents the result of an executed [ImageRequest].
 *
 * @see ImageLoader.enqueue
 * @see ImageLoader.execute
 */
sealed class ImageResult {
    abstract val bitmap: ImageBitmap?
    abstract val request: ImageRequest
}

/**
 * Indicates that the request completed successfully.
 */
class SuccessResult(
    /**
     * The success drawable.
     */
    override val bitmap: ImageBitmap,

    /**
     * The request that was executed to create this result.
     */
    override val request: ImageRequest,

    /**
     * The data source that the image was loaded from.
     */
    val dataSource: DataSource,

    /**
     * The cache key for the image in the memory cache.
     * It is 'null' if the image was not written to the memory cache.
     */
    val memoryCacheKey: MemoryCache.Key? = null,

    /**
     * The cache key for the image in the disk cache.
     * It is 'null' if the image was not written to the disk cache.
     */
    val diskCacheKey: String? = null,

    /**
     * 'true' if the image is sampled (i.e. loaded into memory at less than its original size).
     */
    val isSampled: Boolean = false,

    /**
     * 'true' if [ImageRequest.placeholderMemoryCacheKey] was present in the memory cache.
     */
    val isPlaceholderCached: Boolean = false,
) : ImageResult() {

    fun copy(
        drawable: ImageBitmap = this.bitmap,
        request: ImageRequest = this.request,
        dataSource: DataSource = this.dataSource,
        memoryCacheKey: MemoryCache.Key? = this.memoryCacheKey,
        diskCacheKey: String? = this.diskCacheKey,
        isSampled: Boolean = this.isSampled,
        isPlaceholderCached: Boolean = this.isPlaceholderCached,
    ) = SuccessResult(
        bitmap = drawable,
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

/**
 * Indicates that an error occurred while executing the request.
 */
class ErrorResult(
    /**
     * The error drawable.
     */
    override val bitmap: ImageBitmap?,

    /**
     * The request that was executed to create this result.
     */
    override val request: ImageRequest,

    /**
     * The error that failed the request.
     */
    val throwable: Throwable,
) : ImageResult() {

    fun copy(
        drawable: ImageBitmap? = this.bitmap,
        request: ImageRequest = this.request,
        throwable: Throwable = this.throwable,
    ) = ErrorResult(
        bitmap = drawable,
        request = request,
        throwable = throwable,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ErrorResult &&
            bitmap == other.bitmap &&
            request == other.request &&
            throwable == other.throwable
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + throwable.hashCode()
        return result
    }
}
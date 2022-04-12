package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.size.Precision
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * @author: Chen
 * @createTime: 2022/4/11 14:07
 * @description:
 **/
open class DefaultRequestOptions(
    val interceptorDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    val fetcherDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val decoderDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val transformationDispatcher: CoroutineDispatcher = Dispatchers.IO,
//    val transitionFactory: Transition.Factory = Transition.Factory.NONE,
    val precision: Precision = Precision.AUTOMATIC,
    val bitmapConfig: ImageBitmapConfig = ImageBitmapConfig.Argb8888,
    val allowHardware: Boolean = true,
    val allowRgb565: Boolean = false,
    val placeholder: ImageBitmap? = null,
    val error: ImageBitmap? = null,
    val fallback: ImageBitmap? = null,
    val memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    val diskCachePolicy: CachePolicy = CachePolicy.ENABLED,
    val networkCachePolicy: CachePolicy = CachePolicy.ENABLED,
) {

    fun copy(
        interceptorDispatcher: CoroutineDispatcher = this.interceptorDispatcher,
        fetcherDispatcher: CoroutineDispatcher = this.fetcherDispatcher,
        decoderDispatcher: CoroutineDispatcher = this.decoderDispatcher,
        transformationDispatcher: CoroutineDispatcher = this.transformationDispatcher,
//        transitionFactory: Transition.Factory = this.transitionFactory,
        precision: Precision = this.precision,
        bitmapConfig: ImageBitmapConfig = this.bitmapConfig,
        allowHardware: Boolean = this.allowHardware,
        allowRgb565: Boolean = this.allowRgb565,
        placeholder: ImageBitmap? = this.placeholder,
        error: ImageBitmap? = this.error,
        fallback: ImageBitmap? = this.fallback,
        memoryCachePolicy: CachePolicy = this.memoryCachePolicy,
        diskCachePolicy: CachePolicy = this.diskCachePolicy,
        networkCachePolicy: CachePolicy = this.networkCachePolicy,
    ) = DefaultRequestOptions(
        interceptorDispatcher = interceptorDispatcher,
        fetcherDispatcher = fetcherDispatcher,
        decoderDispatcher = decoderDispatcher,
        transformationDispatcher = transformationDispatcher,
//        transitionFactory = transitionFactory,
        precision = precision,
        bitmapConfig = bitmapConfig,
        allowHardware = allowHardware,
        allowRgb565 = allowRgb565,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        memoryCachePolicy = memoryCachePolicy,
        diskCachePolicy = diskCachePolicy,
        networkCachePolicy = networkCachePolicy,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DefaultRequestOptions &&
                interceptorDispatcher == other.interceptorDispatcher &&
                fetcherDispatcher == other.fetcherDispatcher &&
                decoderDispatcher == other.decoderDispatcher &&
                transformationDispatcher == other.transformationDispatcher &&
//                transitionFactory == other.transitionFactory &&
                precision == other.precision &&
                bitmapConfig == other.bitmapConfig &&
                allowHardware == other.allowHardware &&
                allowRgb565 == other.allowRgb565 &&
                placeholder == other.placeholder &&
                error == other.error &&
                fallback == other.fallback &&
                memoryCachePolicy == other.memoryCachePolicy &&
                diskCachePolicy == other.diskCachePolicy &&
                networkCachePolicy == other.networkCachePolicy
    }

    override fun hashCode(): Int {
        var result = interceptorDispatcher.hashCode()
        result = 31 * result + fetcherDispatcher.hashCode()
        result = 31 * result + decoderDispatcher.hashCode()
        result = 31 * result + transformationDispatcher.hashCode()
//        result = 31 * result + transitionFactory.hashCode()
        result = 31 * result + precision.hashCode()
        result = 31 * result + bitmapConfig.hashCode()
        result = 31 * result + allowHardware.hashCode()
        result = 31 * result + allowRgb565.hashCode()
        result = 31 * result + placeholder.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + fallback.hashCode()
        result = 31 * result + memoryCachePolicy.hashCode()
        result = 31 * result + diskCachePolicy.hashCode()
        result = 31 * result + networkCachePolicy.hashCode()
        return result
    }
}
package cn.chitanda.kmmage.request

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.size.Precision
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.ScaleResolver
import cn.chitanda.kmmage.size.SizeResolver
import java.io.File
import java.nio.ByteBuffer

/**
 * @author: Chen
 * @createTime: 2022/4/6 13:54
 * @description:
 **/

class ImageRequest private constructor(
    val data: Any,
    val precision: Precision,
    val bitmapConfig: ImageBitmapConfig,
    val sizeResolver: SizeResolver,
    val scaleResolver: ScaleResolver,
    val memoryCacheKey: MemoryCache.Key?,
    val diskCacheKey: String?,
    val placeholderMemoryCacheKey: MemoryCache.Key?,
    val listener: Listener?,
) {
    interface Listener {
        /**
         * Called immediately after [Target.onStart].
         */
        fun onStart(request: ImageRequest) {}

        /**
         * Called if the request is cancelled.
         */
        fun onCancel(request: ImageRequest) {}

        /**
         * Called if an error occurs while executing the request.
         */
        fun onError(request: ImageRequest, result: ErrorResult)

        /**
         * Called if the request completes successfully.
         */
        fun onSuccess(request: ImageRequest, result: SuccessResult)
    }

    class Builder {

        private var data: Any? = null
        private var precision: Precision? = null
        private var bitmapConfig: ImageBitmapConfig? = null
        private var sizeResolver: SizeResolver? = null
        private var scaleResolver: ScaleResolver? = null
        private var memoryCacheKey: MemoryCache.Key? = null
        private var diskCacheKey: String? = null
        private var placeholderMemoryCacheKey: MemoryCache.Key? = null
        private var listener: Listener? = null
        private var options: DefaultRequestOptions = DefaultRequestOptions()

        constructor()
        constructor(imageRequest: ImageRequest) {
            data = imageRequest.data
            precision = imageRequest.precision
            bitmapConfig = imageRequest.bitmapConfig
            sizeResolver = imageRequest.sizeResolver
            scaleResolver = imageRequest.scaleResolver
            memoryCacheKey = imageRequest.memoryCacheKey
            diskCacheKey = imageRequest.diskCacheKey
            placeholderMemoryCacheKey = imageRequest.placeholderMemoryCacheKey
            listener = imageRequest.listener
        }


        /**
         * Set the data to load.
         *
         * The default supported data types are:
         * - [String] (mapped to a [Uri])
         * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
         * - [HttpUrl]
         * - [File]
         * - [DrawableRes]
         * - [Drawable]
         * - [Bitmap]
         * - [ByteBuffer]
         * - [Resources] (compose for desktop resource files)
         */
        fun data(data: Any?) = apply {
            this.data = data
        }

        /**
         * set the precision of the size of the loaded image
         * default value is [Precision.AUTOMATIC]
         */
        fun precision(precision: Precision) = apply {
            this.precision = precision
        }

        /**
         * set the bitmap config of the image
         */
        fun bitmapConfig(bitmapConfig: ImageBitmapConfig) = apply {
            this.bitmapConfig = bitmapConfig
        }

        fun sizeResolver(sizeResolver: SizeResolver) = apply {
            this.sizeResolver = sizeResolver
        }

        fun scaleResovler(scaleResolver: ScaleResolver) = apply {
            this.scaleResolver = scaleResolver
        }

        fun memoryCacheKey(key: String?) = memoryCacheKey(key?.let { MemoryCache.Key(it) })

        fun memoryCacheKey(memoryCacheKey: MemoryCache.Key?) = apply {
            this.memoryCacheKey = memoryCacheKey
        }

        fun diskCacheKey(diskCacheKey: String?) = apply {
            this.diskCacheKey = diskCacheKey
        }

        fun placeholderMemoryCacheKey(key: String?) =
            placeholderMemoryCacheKey(key?.let { MemoryCache.Key(it) })

        fun placeholderMemoryCacheKey(key: MemoryCache.Key?) = apply {
            this.placeholderMemoryCacheKey = key
        }

        /**
         * use lambda create and set the [Listener].
         */
        inline fun listener(
            crossinline onStart: (request: ImageRequest) -> Unit = {},
            crossinline onCancel: (request: ImageRequest) -> Unit = {},
            crossinline onError: (request: ImageRequest, result: ErrorResult) -> Unit = { _, _ -> },
            crossinline onSuccess: (request: ImageRequest, result: SuccessResult) -> Unit = { _, _ -> }
        ) = listener(object : Listener {
            override fun onStart(request: ImageRequest) = onStart(request)
            override fun onCancel(request: ImageRequest) = onCancel(request)
            override fun onError(request: ImageRequest, result: ErrorResult) =
                onError(request, result)

            override fun onSuccess(request: ImageRequest, result: SuccessResult) =
                onSuccess(request, result)
        })

        /**
         * set the [Listener].
         */
        fun listener(listener: Listener?) = apply {
            this.listener = listener
        }


        fun build() = ImageRequest(
            data = data ?: NullRequestData,
            precision = precision ?: options.precision,
            bitmapConfig = bitmapConfig ?: options.bitmapConfig,
            sizeResolver = sizeResolver ?: DefaultSizeResolver,
            scaleResolver = scaleResolver ?: ScaleResolver(Scale.FIT),
            memoryCacheKey, diskCacheKey, placeholderMemoryCacheKey, listener
        )
    }
}

object NullRequestData {
    override fun toString() = "cn.chitanda.kmmage.request.NullRequestData"
}

internal expect val DEFAULT_REQUEST_OPTION: DefaultRequestOptions
internal expect val DefaultSizeResolver: SizeResolver
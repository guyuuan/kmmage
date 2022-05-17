package cn.chitanda.kmmage.request

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.size.Precision
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.ScaleResolver
import cn.chitanda.kmmage.size.SizeResolver
import cn.chitanda.kmmage.target.Target
import cn.chitanda.kmmage.transform.Transformation
import cn.chitanda.kmmage.transition.Transition
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
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
    val target: Target?,
    val placeholderMemoryCacheKey: MemoryCache.Key?,
    val listener: Listener?,
    val transformations: List<Transformation>,
    val allowHardware: Boolean,
    val diskCachePolicy: CachePolicy,
    val memoryCachePolicy: CachePolicy,
    val networkCachePolicy: CachePolicy,
    val allowRgb565: Boolean,
    val scale: Scale,
    val premultipliedAlpha: Boolean,
    val headers: Headers,
    val tags: Tags,
    val parameters: Parameters,
    val allowConversionToBitmap: Boolean,
    val lifecycle: Any,
    val context: Any,
    val transitionFactory: Transition.Factory,
) {
    fun newBuilder(context: Any = this.context): Builder = Builder(this, context)

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
        internal val context: Any
        internal var data: Any?
        internal var precision: Precision?
        internal var bitmapConfig: ImageBitmapConfig?
        internal var sizeResolver: SizeResolver?
        internal var scaleResolver: ScaleResolver?
        internal var memoryCacheKey: MemoryCache.Key?
        internal var diskCacheKey: String?
        internal var placeholderMemoryCacheKey: MemoryCache.Key?
        internal var listener: Listener?
        internal var defaults: DefaultRequestOptions = DefaultRequestOptions()
        internal var target: Target?
        internal var transformations: List<Transformation> = emptyList()
        internal var allowHardware: Boolean?
        internal var diskCachePolicy: CachePolicy?
        internal var memoryCachePolicy: CachePolicy?
        internal var networkCachePolicy: CachePolicy?
        internal var allowRgb565: Boolean?
        internal var scale: Scale?
        internal var premultipliedAlpha: Boolean
        internal var headers: HeadersBuilder?
        internal var tags: MutableMap<Class<*>, Any>?
        internal var parameters: Parameters.Builder?
        internal var allowConversionToBitmap: Boolean
        internal var lifecycle: Any?
        internal var transitionFactory: Transition.Factory?

        constructor(context: Any) {
            this.context = context
            data = null
            precision = null
            bitmapConfig = null
            sizeResolver = null
            scaleResolver = null
            memoryCacheKey = null
            diskCacheKey = null
            placeholderMemoryCacheKey = null
            listener = null
            defaults = DefaultRequestOptions()
            target = null
            transformations = emptyList()
            allowHardware = null
            diskCachePolicy = null
            memoryCachePolicy = null
            networkCachePolicy = null
            allowRgb565 = null
            scale = null
            premultipliedAlpha = true
            headers = null
            tags = null
            parameters = null
            allowConversionToBitmap = true
            lifecycle = null
            transitionFactory = null
        }

        constructor(imageRequest: ImageRequest, context: Any = imageRequest) {
            this.context = context
            data = imageRequest.data
            precision = imageRequest.precision
            bitmapConfig = imageRequest.bitmapConfig
            sizeResolver = imageRequest.sizeResolver
            scaleResolver = imageRequest.scaleResolver
            memoryCacheKey = imageRequest.memoryCacheKey
            diskCacheKey = imageRequest.diskCacheKey
            placeholderMemoryCacheKey = imageRequest.placeholderMemoryCacheKey
            listener = imageRequest.listener
            target = imageRequest.target
            transformations = imageRequest.transformations
            allowHardware = imageRequest.allowHardware
            diskCachePolicy = imageRequest.diskCachePolicy
            memoryCachePolicy = imageRequest.memoryCachePolicy
            networkCachePolicy = imageRequest.networkCachePolicy
            allowRgb565 = imageRequest.allowRgb565
            scale = imageRequest.scale
            premultipliedAlpha = imageRequest.premultipliedAlpha
            headers = HeadersBuilder().apply {
                imageRequest.headers.forEach { name, list ->
                    appendAll(name, list)
                }
            }
            tags = imageRequest.tags.asMap().toMutableMap()
            parameters = imageRequest.parameters.newBuilder()
            allowConversionToBitmap = imageRequest.allowConversionToBitmap
            lifecycle = imageRequest.lifecycle
            transitionFactory = imageRequest.transitionFactory
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

        inline fun target(
            crossinline onStart: (placeholder: ImageBitmap?) -> Unit = {},
            crossinline onError: (error: ImageBitmap?) -> Unit = {},
            crossinline onSuccess: (result: ImageBitmap) -> Unit = {}
        ) = target(object : Target {
            override fun onStart(placeholder: ImageBitmap?) = onStart(placeholder)
            override fun onError(error: ImageBitmap?) = onError(error)
            override fun onSuccess(result: ImageBitmap) = onSuccess(result)
        })

        /**
         * Set the [Target].
         */
        fun target(target: Target?) = apply {
            this.target = target
        }

        fun transformations(vararg transformations: Transformation) =
            transformations(transformations.toList())


        fun transformations(transformations: List<Transformation>) = apply {
            this.transformations = transformations
        }

        fun allowHardware(enabled: Boolean) = apply {
            this.allowHardware = enabled
        }

        fun memoryCachePolicy(policy: CachePolicy) = apply {
            this.memoryCachePolicy = policy
        }

        fun diskCachePolicy(policy: CachePolicy) = apply {
            this.diskCachePolicy = policy
        }

        /**
         * Enable/disable reading from the network.
         *
         * NOTE: Disabling writes has no effect.
         */
        fun networkCachePolicy(policy: CachePolicy) = apply {
            this.networkCachePolicy = policy
        }

        fun allowRgb565(allowed: Boolean) = apply { this.allowRgb565 = allowed }
//      premultipliedAlpha = imageRequest.premultipliedAlpha
//            headers = HeadersBuilder().apply {
//                imageRequest.headers.forEach { name, list ->
//                    appendAll(name, list)
//                }
//            }
//            tags = imageRequest.tags.asMap().toMutableMap()
//            parameters = imageRequest.parameters.newBuilder()

        fun premultipliedAlpha(enabled: Boolean) = apply {
            this.premultipliedAlpha = enabled
        }

        fun headers(headers: Headers) = apply {
            this.headers = HeadersBuilder().apply {
                headers.forEach { name, list ->
                    appendAll(name, list)
                }
            }
        }

        /**
         * Add a header for any network operations performed by this request.
         *
         * @see Headers.Builder.add
         */
        fun addHeader(name: String, value: String) = apply {
            (this.headers ?: HeadersBuilder().also { this.headers = it }).append(name, value)
        }

        /**
         * Set a header for any network operations performed by this request.
         *
         * @see Headers.Builder.set
         */
        fun setHeader(name: String, value: String) = apply {
            (this.headers ?: HeadersBuilder().also { this.headers = it })[name] = value
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHeader(name: String) = apply {
            this.headers?.remove(name)
        }

        /**
         * Attach [tag] to this request using [T] as the key.
         */
        inline fun <reified T : Any> tag(tag: T?) = tag(T::class.java, tag)

        fun <T : Any> tag(type: Class<in T>, tag: T?) = apply {
            if (tag == null) {
                this.tags?.remove(type)
            } else {
                val tags = this.tags ?: mutableMapOf<Class<*>, Any>().also { this.tags = it }
                tags[type] = type.cast(tag)!!
            }
        }


        fun tags(tags: Tags) = apply {
            this.tags = tags.asMap().toMutableMap()
        }

        fun allowConversionToBitmap(enable: Boolean) = apply {
            this.allowConversionToBitmap = true
        }

        fun defaults(defaults: DefaultRequestOptions) = apply {
            this.defaults = defaults
        }

        fun build() = ImageRequest(
            context = context,
            data = data ?: NullRequestData,
            precision = precision ?: defaults.precision,
            bitmapConfig = bitmapConfig ?: defaults.bitmapConfig,
            sizeResolver = sizeResolver ?: DefaultSizeResolver,
            scaleResolver = scaleResolver ?: ScaleResolver(Scale.FIT),
            memoryCacheKey = memoryCacheKey,
            diskCacheKey = diskCacheKey,
            target = target,
            placeholderMemoryCacheKey = placeholderMemoryCacheKey,
            listener = listener,
            transformations = transformations,
            allowHardware = allowHardware ?: defaults.allowHardware,
            diskCachePolicy = diskCachePolicy ?: defaults.diskCachePolicy,
            memoryCachePolicy = memoryCachePolicy ?: defaults.memoryCachePolicy,
            networkCachePolicy = networkCachePolicy ?: defaults.networkCachePolicy,
            allowRgb565 = allowRgb565 ?: defaults.allowRgb565,
            scale = scale ?: Scale.FIT,
            premultipliedAlpha = premultipliedAlpha,
            headers = headers?.build() ?: Headers.Empty,
            tags = tags?.let { Tags.from(it) } ?: Tags.EMPTY,
            parameters = parameters?.build() ?: Parameters(),
            allowConversionToBitmap = allowConversionToBitmap,
            lifecycle = lifecycle ?: context.getLifecycle(),
            transitionFactory = transitionFactory ?: defaults.transitionFactory
        )

    }

}

object NullRequestData {
    override fun toString() = "cn.chitanda.kmmage.request.NullRequestData"
}

internal expect val DEFAULT_REQUEST_OPTION: DefaultRequestOptions
internal expect val DefaultSizeResolver: SizeResolver

internal expect fun Any.getLifecycle(): Any
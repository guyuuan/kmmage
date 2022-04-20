package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.Size
import io.ktor.http.Headers

/**
 * @author: Chen
 * @createTime: 2022/4/20 17:23
 * @description:
 **/

/**
 * A set of configuration options for fetching and decoding an image.
 * [Fetcher]s and [Decoder]s should respect these options as best as possible.
 */
data class Options(

    /**
     * The requested config for any [Bitmap]s.
     */
    val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888,

    /**
     * The requested output size for the image request.
     */
    val size: Size = Size.ORIGINAL,

    /**
     * The scaling algorithm for how to fit the source image's dimensions into the target's
     * dimensions.
     */
    val scale: Scale = Scale.FIT,

    /**
     * 'true' if the output image does not need to fit/fill the target's dimensions exactly.
     * For instance, if 'true' [Decoder] will not decode an image at a larger size
     * than its source dimensions as an optimization.
     */
    val allowInexactSize: Boolean = false,

    /**
     * 'true' if a component is allowed to use [Bitmap.Config.RGB_565] as an optimization.
     * As RGB_565 does not have an alpha channel, components should only use RGB_565 if the
     * image is guaranteed to not use alpha.
     */
    val allowRgb565: Boolean = false,

    /**
     * 'true' if the color (RGB) channels of the decoded image should be pre-multiplied by the
     * alpha channel. The default behavior is to enable pre-multiplication but in some environments
     * it can be necessary to disable this feature to leave the source pixels unmodified.
     */
    val premultipliedAlpha: Boolean = true,

    /**
     * The cache key to use when persisting images to the disk cache or 'null' if the component can
     * compute its own.
     */
    val diskCacheKey: String? = null,

    /**
     * The header fields to use for any network requests.
     */
    val headers: Headers = Headers.Empty,

    /**
     * A map of custom objects. These are used to attach custom data to a request.
     */
    val tags: Tags = Tags.EMPTY,

    /**
     * A map of custom parameters. These are used to pass custom data to a component.
     */
    val parameters: Parameters = Parameters.EMPTY,

    /**
     * Determines if this request is allowed to read/write from/to memory.
     */
    val memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,

    /**
     * Determines if this request is allowed to read/write from/to disk.
     */
    val diskCachePolicy: CachePolicy = CachePolicy.ENABLED,

    /**
     * Determines if this request is allowed to read from the network.
     */
    val networkCachePolicy: CachePolicy = CachePolicy.ENABLED,
)
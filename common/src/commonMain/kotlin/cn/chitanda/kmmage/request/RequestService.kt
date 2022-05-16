package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.size.Dimension
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.util.HardwareBitmapService
import cn.chitanda.kmmage.util.SystemCallback
import cn.chitanda.kmmage.util.VALID_TRANSFORMATION_CONFIGS
import cn.chitanda.kmmage.util.allowInexactSize
import cn.chitanda.kmmage.util.isHardware

/**
 * @author: Chen
 * @createTime: 2022/5/13 11:44
 * @description:
 **/
internal class RequestService(
    private val imageLoader: ImageLoader,
    private val systemCallback: SystemCallback
) {
    private val hardwareBitmapService = HardwareBitmapService()

    fun options(request: ImageRequest, size: Size): Options {
        val isValidConfig = isConfigValidForTransformations(request) &&
                isConfigValidForHardwareAllocation(request, size)
        val config = if (isValidConfig) request.bitmapConfig else ImageBitmapConfig.Argb8888
        val networkCachePolicy = if (systemCallback.isOnline) {
            request.networkCachePolicy
        } else {
            CachePolicy.DISABLED
        }
        val allowRgb565 = request.allowRgb565 && request.transformations.isEmpty() &&
                config != ImageBitmapConfig.Rgb565
        val scale = if (size.width == Dimension.Undefined || size.height == Dimension.Undefined) {
            Scale.FIT
        } else {
            request.scale
        }
        return Options(
            config = config,
            size = size,
            scale = scale,
            allowInexactSize = request.allowInexactSize,
            allowRgb565 = allowRgb565,
            premultipliedAlpha = request.premultipliedAlpha,
            diskCacheKey = request.diskCacheKey,
            headers = request.headers,
            tags = request.tags,
            parameters = request.parameters,
            memoryCachePolicy = request.memoryCachePolicy,
            diskCachePolicy = request.diskCachePolicy,
            networkCachePolicy = networkCachePolicy
        )
    }

    fun errorResult(request: ImageRequest, throwable: Throwable): ErrorResult {
        return ErrorResult(
            request = request,
            throwable = throwable
        )
    }

    fun allowHardwareWorkerThread(options: Options): Boolean {
        return !options.config.isHardware || hardwareBitmapService.allowHardwareWorkerThread()
    }

    fun isConfigValidForHardware(
        request: ImageRequest,
        requestedConfig: ImageBitmapConfig
    ): Boolean {
        // Short circuit if the requested bitmap config is software.
        if (!requestedConfig.isHardware) return true

        // Ensure the request allows hardware bitmaps.
        if (!request.allowHardware) return false

        return true
    }

    /**
     * Return 'true' if [request]'s requested bitmap config is valid (i.e. can be returned to its
     * [Target]).
     *
     * This check is similar to [isConfigValidForHardware] except this method also checks
     * that we are able to allocate a new hardware bitmap.
     */
    private fun isConfigValidForHardwareAllocation(request: ImageRequest, size: Size): Boolean {
        return isConfigValidForHardware(request, request.bitmapConfig) &&
                hardwareBitmapService.allowHardwareMainThread(size)
    }

    /** Return 'true' if [ImageRequest.bitmapConfig] is valid given its [Transformation]s. */
    private fun isConfigValidForTransformations(request: ImageRequest): Boolean {
        return request.transformations.isEmpty() ||
                request.bitmapConfig in VALID_TRANSFORMATION_CONFIGS
    }
}
package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.size.Precision
import cn.chitanda.kmmage.size.SizeResolver

/**
 * @author: Chen
 * @createTime: 2022/4/6 13:54
 * @description:
 **/

class ImageRequest private constructor(
    val data: Any,
    val precision: Precision,
    val bitmapConfig: ImageBitmapConfig,
    val colorSpace:ColorSpace,
    val sizeResolver: SizeResolver,
    val scaleResolver: SizeResolver,
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
        fun onError(request: ImageRequest, result: ErrorResult) {}

        /**
         * Called if the request completes successfully.
         */
        fun onSuccess(request: ImageRequest, result: SuccessResult) {}
    }

    class Builder{

    }
}
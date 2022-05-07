package cn.chitanda.kmmage

import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.EventListener.Factory
import cn.chitanda.kmmage.decode.DecodeResult
import cn.chitanda.kmmage.decode.Decoder
import cn.chitanda.kmmage.fetch.FetchResult
import cn.chitanda.kmmage.fetch.Fetcher
import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.transition.Transition

/**
 * @author: Chen
 * @createTime: 2022/5/7 17:47
 * @description:
 **/
interface EventListener : ImageRequest.Listener {

    override fun onStart(request: ImageRequest) {}


    fun resolveSizeStart(request: ImageRequest) {}


    fun resolveSizeEnd(request: ImageRequest, size: Size) {}


    fun mapStart(request: ImageRequest, input: Any) {}


    fun mapEnd(request: ImageRequest, output: Any) {}

    fun keyStart(request: ImageRequest, input: Any) {}

    fun keyEnd(request: ImageRequest, output: String?) {}


    fun fetchStart(request: ImageRequest, fetcher: Fetcher, options: Options) {}


    fun fetchEnd(request: ImageRequest, fetcher: Fetcher, options: Options, result: FetchResult?) {}


    fun decodeStart(request: ImageRequest, decoder: Decoder, options: Options) {}


    fun decodeEnd(
        request: ImageRequest,
        decoder: Decoder,
        options: Options,
        result: DecodeResult?
    ) {
    }


    fun transformStart(request: ImageRequest, input: ImageBitmap) {}


    fun transformEnd(request: ImageRequest, output: ImageBitmap) {}


    fun transitionStart(request: ImageRequest, transition: Transition) {}


    fun transitionEnd(request: ImageRequest, transition: Transition) {}


    override fun onCancel(request: ImageRequest) {}


    override fun onError(request: ImageRequest, result: ErrorResult) {}


    override fun onSuccess(request: ImageRequest, result: SuccessResult) {}

    fun interface Factory {
        fun create(imageRequest: ImageRequest): EventListener

        companion object {
            @JvmStatic
            val NONE = Factory { EventListener.NONE }
        }
    }

    companion object {
        @JvmStatic
        val NONE = object : EventListener {}
    }
}
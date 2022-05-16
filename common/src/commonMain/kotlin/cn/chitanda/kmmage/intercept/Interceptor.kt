package cn.chitanda.kmmage.intercept

import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.size.Size

/**
 * @author: Chen
 * @createTime: 2022/4/29 10:56
 * @description:
 **/
interface Interceptor {

    suspend fun intercept(chain: Chain): ImageResult

    interface Chain {

        val request:ImageRequest

        val size :Size

        /**
         * set the requested [size] to the loaded image
         */
        fun withSize(size: Size):Chain

        /**
         * continue executing the chain
         */
        suspend fun proceed(request: ImageRequest):ImageResult
    }
}
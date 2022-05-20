package cn.chitanda.kmmage.decode

import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.request.Options

/**
 * @author: Chen
 * @createTime: 2022/4/29 15:49
 * @description:
 **/
interface Decoder {
    suspend fun decode(): DecodeResult?

    interface Factory {
        fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder?
    }
}
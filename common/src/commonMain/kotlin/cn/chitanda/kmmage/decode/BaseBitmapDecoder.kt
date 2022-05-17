package cn.chitanda.kmmage.decode

import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.toImageBitmap
import okio.BufferedSource
import okio.buffer

/**
 * @author: Chen
 * @createTime: 2022/5/17 18:10
 * @description:
 **/
class BaseBitmapDecoder(private val source: ImageSource) : Decoder {
    override suspend fun decode(): DecodeResult {
        return DecodeResult(bitmap = source.source().toImageBitmap(), isSampled = false)
    }

    class Factory : Decoder.Factory {
        override fun create(
            source: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder {
            return BaseBitmapDecoder(source.source)
        }
    }
}
package cn.chitanda.kmmage.decode

import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.toImageBitmap
import io.ktor.utils.io.makeShared
import okio.BufferedSource
import okio.buffer
import okio.source

/**
 * @author: Chen
 * @createTime: 2022/5/17 18:10
 * @description:
 **/
class BaseBitmapDecoder(private val source: ImageSource) : Decoder {
    override suspend fun decode(): DecodeResult {
       val buffer= source.file().toFile().inputStream().source().buffer()
        return DecodeResult(bitmap =  buffer.toImageBitmap(), isSampled = false)
    }

    class Factory : Decoder.Factory {
        override fun create(
            source: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder {
            println("data source type = ${source.dataSource}")
            return BaseBitmapDecoder(source.source)
        }
    }
}
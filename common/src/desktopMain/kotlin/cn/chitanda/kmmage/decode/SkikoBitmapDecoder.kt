package cn.chitanda.kmmage.decode

import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.chitanda.kmmage.request.Options
import net.coobird.thumbnailator.Thumbnails
import java.io.IOException

/**
 * @author: Chen
 * @createTime: 2022/5/6 09:57
 * @description:
 **/
class SkikoBitmapDecoder(
    private val imageSource: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        try {
            val image =
                Thumbnails.of(imageSource.source().inputStream()).size(100, 100).asBufferedImage()
            return DecodeResult(image.toComposeImageBitmap(), true)
        } catch (e: IllegalArgumentException) {

        } catch (e: IOException) {
        }
        return null
    }

}
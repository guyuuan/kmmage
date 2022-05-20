package cn.chitanda.kmmage.decode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.util.androidContext
import cn.chitanda.kmmage.util.heightPx
import cn.chitanda.kmmage.util.toAndroidBitmapConfig
import cn.chitanda.kmmage.util.widthPx
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.buffer
import kotlin.math.roundToInt

/**
 * @author: Chen
 * @createTime: 2022/5/20 11:55
 * @description:
 **/
class BitmapFactoryDecoder constructor(
    private val source: ImageSource,
    private val options: Options,
    private val parallelismLock: Semaphore = Semaphore(Int.MAX_VALUE)
) : Decoder {
    override suspend fun decode(): DecodeResult = parallelismLock.withPermit {
        runInterruptible {
            BitmapFactory.Options().decode()
        }
    }

    private fun BitmapFactory.Options.decode(): DecodeResult {
        val safeSource = ExceptionCatchingSource(source.source())
        val safeBufferedSource = safeSource.buffer()
        inJustDecodeBounds = true
        BitmapFactory.decodeStream(safeBufferedSource.peek().inputStream(), null, this)
        safeSource.exception?.let { throw  it }
        inJustDecodeBounds = false
        inMutable = false
        if (Build.VERSION.SDK_INT >= 26 && options.colorSpace != null) {
            // TODO:  support Color Space
        }
        inPremultiplied = options.premultipliedAlpha
        configureConfig()
        configureScale()
        val outBitmap: Bitmap? = safeBufferedSource.use {
            BitmapFactory.decodeStream(it.inputStream(), null, this)
        }
        safeSource.exception?.let { throw it }
        checkNotNull(outBitmap) {
            "BitmapFactory returned a null bitmap. Often this means BitmapFactory could not " +
                    "decode the image data read from the input source (e.g. network, disk, or " +
                    "memory) as it's not encoded as a valid image format."
        }
        outBitmap.density = options.androidContext.resources.displayMetrics.densityDpi
        return DecodeResult(
            bitmap = outBitmap.asImageBitmap(),
            isSampled = inSampleSize > 1 || inScaled
        )
    }

    private fun BitmapFactory.Options.configureConfig() {
        var config = options.config
        if (options.allowRgb565 && config == ImageBitmapConfig.Rgb565) {
            config = ImageBitmapConfig.Rgb565
        }
        if (Build.VERSION.SDK_INT >= 26 && outConfig == Bitmap.Config.RGBA_F16 && config != ImageBitmapConfig.Gpu) {
            config = ImageBitmapConfig.F16
        }

        inPreferredConfig = config.toAndroidBitmapConfig()
    }

    private fun BitmapFactory.Options.configureScale() {
        if (outWidth <= 0 || outHeight <= 0) {
            inSampleSize = 1
            inScaled = false
            return
        }
        val srcWidth = outWidth
        val srcHeight = outHeight
        val dstWidth = options.size.widthPx(options.scale) { srcWidth }
        val dstHeight = options.size.heightPx(options.scale) { srcHeight }
        inSampleSize = DecodeUtils.calculateInSampleSize(
            srcWidth = srcWidth,
            srcHeight = srcHeight,
            dstWidth = dstWidth,
            dstHeight = dstHeight,
            scale = options.scale
        )
        var scale = DecodeUtils.computeSizeMultiplier(
            srcWidth = srcWidth / inSampleSize.toDouble(),
            srcHeight = srcHeight / inSampleSize.toDouble(),
            dstWidth = dstWidth.toDouble(),
            dstHeight = dstHeight.toDouble(),
            scale = options.scale
        )
        if (options.allowInexactSize) {
            scale = scale.coerceAtMost(1.0)
        }
        inScaled = scale != 1.0

        if (inScaled) {
            if (scale > 1) {
                // Upscale
                inDensity = (Int.MAX_VALUE / scale).roundToInt()
                inTargetDensity = Int.MAX_VALUE
            } else {
                // Downscale
                inDensity = Int.MAX_VALUE
                inTargetDensity = (Int.MAX_VALUE * scale).roundToInt()
            }
        }
    }

    class Factory(maxParallelism: Int = DEFAULT_MAX_PARALLELISM) : Decoder.Factory {

        private val parallelismLock = Semaphore(maxParallelism)

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder {
            return BitmapFactoryDecoder(result.source, options, parallelismLock)
        }

        override fun equals(other: Any?) = other is Factory

        override fun hashCode() = javaClass.hashCode()
    }

    internal companion object {
        internal const val DEFAULT_MAX_PARALLELISM = 4
    }

    private class ExceptionCatchingSource(delegate: Source) : ForwardingSource(delegate) {
        var exception: Exception? = null
            private set

        override fun read(sink: Buffer, byteCount: Long): Long {
            try {
                return super.read(sink, byteCount)
            } catch (e: Exception) {
                exception = e
                throw e
            }
        }
    }
}
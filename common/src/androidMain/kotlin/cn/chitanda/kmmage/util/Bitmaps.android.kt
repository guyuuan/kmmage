package cn.chitanda.kmmage.util

import android.graphics.Bitmap
import android.os.Build.VERSION.SDK_INT
import androidx.compose.ui.graphics.ImageBitmapConfig

internal actual val VALID_TRANSFORMATION_CONFIGS: Array<ImageBitmapConfig>
    get() = if (SDK_INT >= 26) {
        arrayOf(ImageBitmapConfig.F16, ImageBitmapConfig.Argb8888)
    } else {
        arrayOf(ImageBitmapConfig.Argb8888)
    }

internal fun ImageBitmapConfig.toAndroidBitmapConfig(): Bitmap.Config? {
    return when {
        this == ImageBitmapConfig.F16 && SDK_INT >= 26 -> Bitmap.Config.RGBA_F16
        this == ImageBitmapConfig.Gpu -> Bitmap.Config.HARDWARE
        this == ImageBitmapConfig.Argb8888 -> Bitmap.Config.ARGB_8888
        this == ImageBitmapConfig.Rgb565 -> Bitmap.Config.RGB_565
        else -> Bitmap.Config.ALPHA_8
    }
}

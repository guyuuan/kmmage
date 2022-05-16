package cn.chitanda.kmmage.util

import android.os.Build.VERSION.SDK_INT
import androidx.compose.ui.graphics.ImageBitmapConfig

internal actual val VALID_TRANSFORMATION_CONFIGS: Array<ImageBitmapConfig>
    get() = if (SDK_INT >= 26) {
        arrayOf(ImageBitmapConfig.F16, ImageBitmapConfig.Argb8888)
    } else {
        arrayOf(ImageBitmapConfig.Argb8888)
    }
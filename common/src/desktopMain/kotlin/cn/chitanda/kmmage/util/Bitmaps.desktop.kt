package cn.chitanda.kmmage.util

import androidx.compose.ui.graphics.ImageBitmapConfig

internal actual val VALID_TRANSFORMATION_CONFIGS: Array<ImageBitmapConfig>
    get() =  arrayOf(ImageBitmapConfig.F16,ImageBitmapConfig.Argb8888)
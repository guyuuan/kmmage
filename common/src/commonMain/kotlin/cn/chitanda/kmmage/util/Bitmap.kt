package cn.chitanda.kmmage.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig

/**
 * @author: Chen
 * @createTime: 2022/3/14 16:28
 * @description:
 **/

internal val ImageBitmap.allocationByteCountCompat: Int
    get() = width * height * config.bytesPerPixel

internal val ImageBitmapConfig.bytesPerPixel: Int
    get() = when {
        this == ImageBitmapConfig.Alpha8 -> 1
        this == ImageBitmapConfig.Argb8888 -> 4
        this == ImageBitmapConfig.Rgb565 -> 2
        this == ImageBitmapConfig.F16 -> 8
        else -> 4
    }



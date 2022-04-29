package cn.chitanda.kmmage.decode

import androidx.compose.ui.graphics.ImageBitmap

/**
 * @author: Chen
 * @createTime: 2022/4/29 15:50
 * @description:
 **/
data class DecodeResult(
    val bitmap: ImageBitmap,
    val isSample: Boolean
)

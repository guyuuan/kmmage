package cn.chitanda.kmmage.transform

import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.size.Size

interface Transformation {

    val cacheKey: String

    suspend fun transform(input: ImageBitmap, size: Size): ImageBitmap
}
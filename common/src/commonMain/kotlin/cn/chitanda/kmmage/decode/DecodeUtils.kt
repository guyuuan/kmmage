package cn.chitanda.kmmage.decode

import androidx.annotation.Px
import cn.chitanda.kmmage.size.Scale
import kotlin.math.max
import kotlin.math.min

/**
 * @author: Chen
 * @createTime: 2022/5/16 14:27
 * @description:
 **/
object DecodeUtils {
    @JvmStatic
    fun computeSizeMultiplier(
        @Px srcWidth: Int,
        @Px srcHeight: Int,
        @Px dstWidth: Int,
        @Px dstHeight: Int,
        scale: Scale
    ): Double {
        val widthPercent = dstWidth / srcWidth.toDouble()
        val heightPercent = dstHeight / srcHeight.toDouble()
        return when (scale) {
            Scale.FILL -> max(widthPercent, heightPercent)
            Scale.FIT -> min(widthPercent, heightPercent)
        }
    }

}
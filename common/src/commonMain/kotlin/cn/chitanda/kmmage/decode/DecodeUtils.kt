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
    fun calculateInSampleSize(
        @Px srcWidth: Int,
        @Px srcHeight: Int,
        @Px dstWidth: Int,
        @Px dstHeight: Int,
        scale: Scale
    ): Int {
        val widthInSampleSize = Integer.highestOneBit(srcWidth / dstWidth)
        val heightInSampleSize = Integer.highestOneBit(srcHeight / dstHeight)
        return when (scale) {
            Scale.FILL -> min(widthInSampleSize, heightInSampleSize)
            Scale.FIT -> max(widthInSampleSize, heightInSampleSize)
        }.coerceAtLeast(1)
    }

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

    @JvmStatic
    fun computeSizeMultiplier(
        @Px srcWidth: Float,
        @Px srcHeight: Float,
        @Px dstWidth: Float,
        @Px dstHeight: Float,
        scale: Scale
    ): Float {
        val widthPercent = dstWidth / srcWidth
        val heightPercent = dstHeight / srcHeight
        return when (scale) {
            Scale.FILL -> max(widthPercent, heightPercent)
            Scale.FIT -> min(widthPercent, heightPercent)
        }
    }

    @JvmStatic
    fun computeSizeMultiplier(
        @Px srcWidth: Double,
        @Px srcHeight: Double,
        @Px dstWidth: Double,
        @Px dstHeight: Double,
        scale: Scale
    ): Double {
        val widthPercent = dstWidth / srcWidth
        val heightPercent = dstHeight / srcHeight
        return when (scale) {
            Scale.FILL -> max(widthPercent, heightPercent)
            Scale.FIT -> min(widthPercent, heightPercent)
        }
    }
}
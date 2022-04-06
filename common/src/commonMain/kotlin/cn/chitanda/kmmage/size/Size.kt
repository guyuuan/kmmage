package cn.chitanda.kmmage.size

import androidx.annotation.Px

/**
 * @author: Chen
 * @createTime: 2022/4/6 10:19
 * @description:
 **/
class Size(val width: Dimension, val height: Dimension) {
    constructor(@Px width: Int, @Px height: Int) : this(Dimension(width), Dimension(height))

    companion object {
        /*
        * A [Size] whose width and height use the source image original size
        * */
        @JvmField
        val ORIGINAL = Size(Dimension.Original, Dimension.Original)
    }

    val isOriginal: Boolean
        get() = this == ORIGINAL
}
package cn.chitanda.kmmage.size

import androidx.annotation.Px

/**
 * @author: Chen
 * @createTime: 2022/4/6 10:19
 * @description:
 **/
sealed class Dimension {
    class Pixel(@Px val px: Int) : Dimension() {
        init {
            require(px > 0) { "px must > 0" }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Pixel && other.px == this.px
        }

        override fun hashCode(): Int = px


        override fun toString(): String =
            "Dimension.Pixels(px=$px)"

    }

    /*
    *  use the original size of the source image
    * */
    object Original : Dimension() {
        override fun toString() = "Dimension.Original"
    }
}

fun Dimension(@Px px: Int) = Dimension.Pixel(px)
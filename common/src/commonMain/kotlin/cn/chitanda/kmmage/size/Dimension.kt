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

    /**
     * Represents an undefined pixel value.
     *
     * E.g. given `Size(400, Dimension.Undefined)`, the image should be loaded to fit/fill a width
     * of 400 pixels irrespective of the image's height.
     *
     * This value is typically used in cases where a dimension is unbounded (e.g. [WRAP_CONTENT],
     * `Constraints.Infinity`).
     *
     * NOTE: If either dimension is [Undefined], [Options.scale] is always [Scale.FIT].
     */
    object Undefined : Dimension() {
        override fun toString() = "Dimension.Undefined"
    }
}

fun Dimension(@Px px: Int) = Dimension.Pixel(px)

inline fun Dimension.pxOrElse(block: () -> Int): Int {
    return if (this is Dimension.Pixel) px else block()
}
package cn.chitanda.kmmage.decode

import androidx.compose.ui.graphics.ImageBitmap

/**
 * The result of [Decoder.decode].
 *
 * @param bitmap The decoded [Drawable].
 * @param isSampled 'true' if [bitmap] is sampled (i.e. loaded into memory at less than its
 *  original size).
 *
 * @see Decoder
 */
class DecodeResult(
    val bitmap: ImageBitmap,
    val isSampled: Boolean,
) {

    fun copy(
        drawable: ImageBitmap = this.bitmap,
        isSampled: Boolean = this.isSampled,
    ) = DecodeResult(
        bitmap = drawable,
        isSampled = isSampled
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DecodeResult &&
            bitmap == other.bitmap &&
            isSampled == other.isSampled
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + isSampled.hashCode()
        return result
    }
}
package cn.chitanda.kmmage.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.times
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import cn.chitanda.kmmage.ComponentRegistry
import cn.chitanda.kmmage.EventListener
import cn.chitanda.kmmage.decode.Decoder
import cn.chitanda.kmmage.disk.DiskCache
import cn.chitanda.kmmage.fetch.Fetcher
import cn.chitanda.kmmage.intercept.Interceptor
import cn.chitanda.kmmage.intercept.RealInterceptorChain
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.size.Scale
import io.ktor.http.ContentType
import java.io.Closeable
import kotlin.math.roundToInt

/**
 * @author: Chen
 * @createTime: 2022/3/14 15:50
 * @description:
 **/
internal inline val Any.identityHashCode: Int
    get() = System.identityHashCode(this)

internal fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {
    }
}

internal fun String.toContentTypeOrNull(): ContentType? {
    return try {
        ContentType.parse(this)
    } catch (_: Exception) {
        null
    }
}


fun String?.toNonNegativeInt(defaultValue: Int): Int {
    try {
        val value = this?.toLong() ?: return defaultValue
        return when {
            value > Int.MAX_VALUE -> Int.MAX_VALUE
            value < 0 -> 0
            else -> value.toInt()
        }
    } catch (_: NumberFormatException) {
        return defaultValue
    }
}

internal fun DiskCache.Editor.abortQuietly() {
    try {
        abort()
    } catch (_: Exception) {
    }
}

internal const val ASSET_FILE_PATH_ROOT = "android_asset"

internal val Interceptor.Chain.eventListener: EventListener
    get() = if (this is RealInterceptorChain) eventListener else EventListener.NONE

internal fun Int.isMinOrMax() = this == Int.MIN_VALUE || this == Int.MAX_VALUE
internal val Interceptor.Chain.isPlaceholderCached: Boolean
    get() = this is RealInterceptorChain && isPlaceholderCached

internal inline fun ComponentRegistry.Builder.addFirst(
    pair: Pair<Fetcher.Factory<out Any>, Class<out Any>>?
) = apply {
    if (pair != null) fetcherFactories.add(0, pair)
}

internal inline fun ComponentRegistry.Builder.addFirst(
    factory: Decoder.Factory?
) = apply { if (factory != null) decoderFactories.add(0, factory) }

internal inline operator fun MemoryCache.get(key: MemoryCache.Key?) = key?.let(::get)
@Stable
internal fun ContentScale.toScale() = when (this) {
    ContentScale.Fit, ContentScale.Inside -> Scale.FIT
    else -> Scale.FILL
}

@Composable
@ReadOnlyComposable
internal expect fun requestOf(data: Any?): ImageRequest

internal fun Constraints.constrainWidth(width: Float) =
    width.coerceIn(minWidth.toFloat(), maxWidth.toFloat())

internal fun Constraints.constrainHeight(height: Float) =
    height.coerceIn(minHeight.toFloat(), maxHeight.toFloat())

internal inline fun Float.takeOrElse(block: () -> Float) = if (isFinite()) this else block()

internal fun Size.toIntSize() = IntSize(width.roundToInt(), height.roundToInt())

@Stable
operator fun Size.times(scaleFactor: ScaleFactor): Size =
    Size(this.width * scaleFactor.scaleX, this.height * scaleFactor.scaleY)

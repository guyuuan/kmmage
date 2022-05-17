package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.ComponentRegistry
import cn.chitanda.kmmage.EventListener
import cn.chitanda.kmmage.decode.Decoder
import cn.chitanda.kmmage.disk.DiskCache
import cn.chitanda.kmmage.fetch.Fetcher
import cn.chitanda.kmmage.intercept.Interceptor
import cn.chitanda.kmmage.intercept.RealInterceptorChain
import cn.chitanda.kmmage.memory.MemoryCache
import io.ktor.http.ContentType
import java.io.Closeable

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
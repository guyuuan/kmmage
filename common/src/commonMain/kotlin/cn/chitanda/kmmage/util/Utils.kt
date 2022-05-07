package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.disk.DiskCache
import io.ktor.http.ContentType
import io.ktor.http.Url
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

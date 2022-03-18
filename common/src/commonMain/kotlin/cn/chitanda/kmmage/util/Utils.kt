package cn.chitanda.kmmage.util

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
    } catch (_: Exception) {}
}
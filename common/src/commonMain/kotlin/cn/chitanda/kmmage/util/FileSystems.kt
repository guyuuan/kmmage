package cn.chitanda.kmmage.util

import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * @author: Chen
 * @createTime: 2022/3/17 16:36
 * @description:
 **/
/** Create a new empty file if one doesn't already exist. */
internal fun FileSystem.createFile(file: Path) {
    if (!exists(file)) sink(file).closeQuietly()
}

/** Tolerant delete, try to clear as many files as possible even after a failure. */
internal fun FileSystem.deleteContents(directory: Path) {
    var exception: IOException? = null
    val files = try {
        list(directory)
    } catch (_: FileNotFoundException) {
        return
    }
    for (file in files) {
        try {
            if (metadata(file).isDirectory) {
                deleteContents(file)
            }
            delete(file)
        } catch (e: IOException) {
            if (exception == null) {
                exception = e
            }
        }
    }
    if (exception != null) {
        throw exception
    }
}

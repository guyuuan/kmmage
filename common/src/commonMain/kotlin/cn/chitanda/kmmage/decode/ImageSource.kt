package cn.chitanda.kmmage.decode

import cn.chitanda.kmmage.util.closeQuietly
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import java.io.Closeable
import java.io.File


/**
 * @author: Chen
 * @createTime: 2022/4/20 14:58
 * @description:
 **/
@JvmName("create")
fun ImageSource(
    file: Path,
    fileSystem: FileSystem = FileSystem.SYSTEM,
    diskCacheKey: String? = null,
    closeable: Closeable? = null,
    metadata: ImageSource.Metadata? = null,
): ImageSource = FileImageSource(
    file = file,
    fileSystem = fileSystem,
    diskCacheKey = diskCacheKey,
    closeable = closeable,
    metadata = metadata
)
@JvmName("create")
fun ImageSource(
    source: BufferedSource,
    cacheDirectory: File,
    metadata: ImageSource.Metadata? = null,
): ImageSource = SourceImageSource(source, cacheDirectory, metadata)

sealed class ImageSource : Closeable {

    /**
     * The [FileSystem] which contains the [file]
     */
    abstract val fileSystem: FileSystem

    /**
     * The [Metadata] of this [ImageSource]
     */
    abstract val metadata: Metadata?

    /**
     * return a [Path] that resolve a file containing data of this [ImageSource]
     */
    abstract fun file(): Path

    /**
     * return a [Path] that resolve a file containing data of this [ImageSource] if one has
     * already been created else return null
     */
    abstract fun fileOrNull(): Path?

    /**
     * return a [BufferedSource] to read this [ImageSource]
     */
    abstract fun source(): BufferedSource

    /**
     * return a [BufferedSource] to read this [ImageSource] if one has
     * already been created else return null
     */
    abstract fun sourceOrNull(): BufferedSource?

    abstract class Metadata
}

class ContentMetadata(val filePath: String) : ImageSource.Metadata()

internal class FileImageSource(
    internal val file: Path,
    internal val diskCacheKey: String?,
    private val closeable: Closeable?,
    override val fileSystem: FileSystem,
    override val metadata: Metadata?
) : ImageSource() {
    private var source: BufferedSource? = null
    private var isClosed = false

    @Synchronized
    override fun file(): Path {
        assertNotClosed()
        return file
    }

    @Synchronized
    override fun fileOrNull() = file()

    @Synchronized
    override fun source(): BufferedSource {
        assertNotClosed()
        source?.let { return it }
        return fileSystem.source(file).buffer().also { source = it }
    }

    @Synchronized
    override fun sourceOrNull(): BufferedSource? {
        assertNotClosed()
        return source
    }

    @Synchronized
    override fun close() {
        isClosed = true
        source?.closeQuietly()
        closeable?.closeQuietly()
    }

    private fun assertNotClosed() {
        check(!isClosed) { "closed" }
    }
}

internal class SourceImageSource(
    source: BufferedSource,
    private val cacheDirectory: File,
    override val metadata: Metadata?
) : ImageSource() {
    private var isClosed = false
    private var file: Path? = null
    private var source: BufferedSource? = source
    override val fileSystem: FileSystem get() = FileSystem.SYSTEM

    @Synchronized
    override fun file(): Path {
        assertNotClosed()
        file?.let { return it }
        val tempFile = File.createTempFile("tmp", null, cacheDirectory).toOkioPath()
        fileSystem.write(tempFile) {
            writeAll(source!!)
        }
        return tempFile.also { file = it }
    }

    @Synchronized
    override fun fileOrNull(): Path? {
        assertNotClosed()
        return file
    }

    @Synchronized
    override fun source(): BufferedSource {
        assertNotClosed()
        source?.let { return it }
        return fileSystem.source(file!!).buffer().also { source = it }
    }

    @Synchronized
    override fun sourceOrNull() = source()

    @Synchronized
    override fun close() {
        isClosed = true
        source?.closeQuietly()
        file?.let(fileSystem::delete)
    }

    private fun assertNotClosed() {
        check(!isClosed) { "closed" }
    }
}
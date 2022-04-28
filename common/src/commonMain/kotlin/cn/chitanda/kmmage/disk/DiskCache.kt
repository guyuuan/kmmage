package cn.chitanda.kmmage.disk

import androidx.annotation.FloatRange
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Closeable
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/3/15 17:02
 * @description: LRU Cache of files
 **/
interface DiskCache {

    /** The current size of the cache in bytes */
    val size: Long

    /** The max size of the cache in bytes */
    val maxSize: Long

    /** The directory where the cache store its data */
    val directory: Path

    /** The file system that contains cache's files */
    val fileSystem: FileSystem

    /** Get the entry associated with [key] */
    operator fun get(key: String): Snapshot?

    /** Edit the entry associated with [key]*/
    fun edit(key: String): Editor?

    /** Delete the entry reference by [key] */
    fun remove(key: String): Boolean

    /** Delete all entries */
    fun clear()


    /**
     * A snapshot of values for an entry.
     * You must read only [metadata] or [data],if you want to modify the contents of those files,
     * you should use [Editor]
     * */
    interface Snapshot : Closeable {
        /** the metadata of this entry */
        val metadata: Path

        /** the data of this entry */
        val data: Path

        /** close snapshot to allow editing */
        override fun close()

        /** close snapshot and return [Editor] for this entry atomically */
        fun closeAndEdit(): Editor?
    }


    interface Editor {
        /** the metadata of this entry */
        val metadata: Path

        /** the data of this entry */
        val data: Path

        /** commit edit so the change can be visible by readers */
        fun commit()

        /** commit edit and return a new [Snapshot] for this entry atomically */
        fun commitAndGet(): Snapshot?

        /** Abort the edit. Any written data will be discarded. */
        fun abort()
    }

    class Builder {

        private var directory: Path? = null
        private var fileSystem = FileSystem.SYSTEM
        private var maxSizePercent = 0.02 // 2%
        private var minimumMaxSizeBytes = 10L * 1024 * 1024 // 10MB
        private var maximumMaxSizeBytes = 250L * 1024 * 1024 // 250MB
        private var maxSizeBytes = 0L
        private var cleanupDispatcher = Dispatchers.IO

        /**
         * Set the [directory] where the cache stores its data.
         *
         * IMPORTANT: It is an error to have two [DiskCache] instances active in the same
         * directory at the same time as this can corrupt the disk cache.
         */
        fun directory(directory: File) = directory(directory.toOkioPath())

        /**
         * Set the [directory] where the cache stores its data.
         *
         * IMPORTANT: It is an error to have two [DiskCache] instances active in the same
         * directory at the same time as this can corrupt the disk cache.
         */
        fun directory(directory: Path) = apply {
            this.directory = directory
        }

        /**
         * Set the [fileSystem] where the cache stores its data, usually [FileSystem.SYSTEM].
         */
        fun fileSystem(fileSystem: FileSystem) = apply {
            this.fileSystem = fileSystem
        }

        /**
         * Set the maximum size of the disk cache as a percentage of the device's free disk space.
         */
        fun maxSizePercent(@FloatRange(from = 0.0, to = 1.0) percent: Double) = apply {
            require(percent in 0.0..1.0) { "size must be in the range [0.0, 1.0]." }
            this.maxSizeBytes = 0
            this.maxSizePercent = percent
        }

        /**
         * Set the minimum size of the disk cache in bytes.
         * This is ignored if [maxSizeBytes] is set.
         */
        fun minimumMaxSizeBytes(size: Long) = apply {
            require(size > 0) { "size must be > 0." }
            this.minimumMaxSizeBytes = size
        }

        /**
         * Set the maximum size of the disk cache in bytes.
         * This is ignored if [maxSizeBytes] is set.
         */
        fun maximumMaxSizeBytes(size: Long) = apply {
            require(size > 0) { "size must be > 0." }
            this.maximumMaxSizeBytes = size
        }

        /**
         * Set the maximum size of the disk cache in bytes.
         */
        fun maxSizeBytes(size: Long) = apply {
            require(size > 0) { "size must be > 0." }
            this.maxSizePercent = 0.0
            this.maxSizeBytes = size
        }

        /**
         * Set the [CoroutineDispatcher] that cache size trim operations will be executed on.
         */
        fun cleanupDispatcher(dispatcher: CoroutineDispatcher) = apply {
            this.cleanupDispatcher = dispatcher
        }

        /**
         * Create a new [DiskCache] instance.
         */
        fun build(): DiskCache {
            val directory = checkNotNull(directory) { "directory == null" }
            val maxSize = if (maxSizePercent > 0) {
                try {
                    val size = maxSizePercent * directory.toFile().totalSpace
                    size.toLong().coerceIn(minimumMaxSizeBytes, maximumMaxSizeBytes)
                } catch (_: Exception) {
                    minimumMaxSizeBytes
                }
            } else {
                maxSizeBytes
            }
            return RealDiskCache(
                maxSize = maxSize,
                directory = directory,
                fileSystem = fileSystem,
                cleanupDispatcher = cleanupDispatcher
            )
        }
    }
}
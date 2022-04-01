package cn.chitanda.kmmage.disk

import okio.Closeable
import okio.FileSystem
import okio.Path

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
}
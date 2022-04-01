package cn.chitanda.kmmage.disk

import kotlinx.coroutines.CoroutineDispatcher
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path

/**
 * @author: Chen
 * @createTime: 2022/4/1 16:15
 * @description:
 **/
class RealDiskCache(
    override val maxSize: Long,
    override val directory: Path,
    override val fileSystem: FileSystem,
    cleanupDispatcher: CoroutineDispatcher
) : DiskCache {
    private val cache = DiskLruCache(
        fileSystem = fileSystem,
        directory = directory,
        maxSize = maxSize,
        cleanUpDispatcher = cleanupDispatcher,
        appVersion = 1,
        valueCount = 2
    )
    override val size get() = cache.size()
    override fun get(key: String): DiskCache.Snapshot? = cache[key.hash()]?.let(::RealSnapshot)

    override fun edit(key: String): DiskCache.Editor? = cache.edit(key.hash())?.let(::RealEditor)

    override fun remove(key: String): Boolean = cache.remove(key.hash())

    override fun clear() {
        cache.evictAll()
    }

    private fun String.hash() = encodeUtf8().sha256().hex()

    private class RealSnapshot(private val snapshot: DiskLruCache.Snapshot) : DiskCache.Snapshot {
        override val metadata: Path
            get() = snapshot.file(ENTRY_METADATA)
        override val data: Path
            get() = snapshot.file(ENTRY_DATA)

        override fun close() = snapshot.close()

        override fun closeAndEdit(): DiskCache.Editor? = snapshot.closeAndEdit()?.let(::RealEditor)

    }

    private class RealEditor(private val editor: DiskLruCache.Editor) : DiskCache.Editor {
        override val metadata: Path
            get() = editor.file(ENTRY_METADATA)
        override val data: Path
            get() = editor.file(ENTRY_DATA)

        override fun commit() = editor.commit()

        override fun commitAndGet() = editor.commitAndGet()?.let(::RealSnapshot)

        override fun abort() = editor.abort()
    }

    companion object {
        private const val ENTRY_METADATA = 0
        private const val ENTRY_DATA = 1
    }
}
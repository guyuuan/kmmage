package cn.chitanda.kmmage.disk

import cn.chitanda.kmmage.util.createFile
import cn.chitanda.kmmage.util.deleteContents
import cn.chitanda.kmmage.util.forEachIndices
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.Closeable
import okio.EOFException
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Sink
import okio.blackholeSink
import okio.buffer
import java.io.Flushable

/**
 * @author: Chen
 * @createTime: 2022/3/17 15:24
 * @description:
 **/
class DiskLruCache(
    fileSystem: FileSystem,
    cleanUpDispatcher: CoroutineDispatcher,
    private val directory: Path,
    private val appVersion: Int,
    private val maxSize: Long,
    private val valueCount: Int
) : Closeable, Flushable {
    init {
        require(maxSize > 0L) { "max size must > 0" }
        require(valueCount > 0) { "value count must > 0" }
    }

    private val journalFile = directory / JOURNAL_FILE
    private val journalFileTmp = directory / JOURNAL_FILE_TMP
    private val journalFileBackup = directory / JOURNAL_FILE_BACKUP
    private val lruEntries = LinkedHashMap<String, Entry>(0, 0.75f, true)
    private val cleanUpScope =
        CoroutineScope(SupervisorJob() + cleanUpDispatcher.limitedParallelism(1))
    private var size = 0L
    private var operationsSinceRewrite = 0
    private var journalWriter: BufferedSink? = null
    private var hasJournalError = false

    private var initialized = false
    private var closed = false
    private var mostRecentTrimFailed = false
    private var mostRecentRebuildFailed = false

    private val fileSystem = object : ForwardingFileSystem(fileSystem) {
        override fun sink(file: Path, mustCreate: Boolean): Sink {
            // Ensure the parent directory exists.
            file.parent?.let(::createDirectories)
            return super.sink(file, mustCreate)
        }
    }

    fun initialize() {
        if (initialized) return

        // if journal.tmp exists ,delete it
        fileSystem.delete(journalFileTmp)

        // if journal.bkp exists use it instead
        if (fileSystem.exists(journalFileBackup)) {
            //if journal also exists ,just delete .bkp
            if (fileSystem.exists(journalFile)) {
                fileSystem.delete(journalFileBackup)
            } else {
                fileSystem.atomicMove(journalFileBackup, journalFile)
            }
        }

        if (fileSystem.exists(journalFile)) {
            try {
                readJournal()
                processJournal()
                initialized = true
                return
            } catch (_: IOException) {

            }
            try {
                delete()
            } finally {
                closed = false
            }
        }
        writeJournal()
        initialized = true
    }

    private fun readJournal() {
        fileSystem.read(journalFile) {
            val magic = readUtf8LineStrict()
            val version = readUtf8LineStrict()
            val appVersionString = readUtf8LineStrict()
            val valueCountString = readUtf8LineStrict()
            val blank = readUtf8LineStrict()

            if (magic != MAGIC || version != VERSION || appVersionString != appVersion.toString() || valueCountString != valueCount.toString() || blank.isNotEmpty()) {
                throw IOException(
                    "journal header is unexpected : " +
                            "[$magic, $version, $appVersionString, $valueCountString, $blank]"
                )
            }
            var lineCount = 0
            while (true) {
                try {
                    readJournalLine(readUtf8LineStrict())
                    lineCount++
                } catch (_: EOFException) {
                    break
                }
            }
            operationsSinceRewrite = lineCount - lruEntries.size

            if (!exhausted()) {
                writeJournal()
            } else {
                journalWriter = newJournalWriter()
            }
        }
    }

    private fun processJournal() {
        var size = 0L
        val iterator = lruEntries.values.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.currentEditor == null) {
                for (i in 0 until valueCount) {
                    size += entry.lengths[i]
                }
            } else {
                entry.currentEditor = null
                for (i in 0 until valueCount) {
                    fileSystem.delete(entry.cleanFiles[i])
                    fileSystem.delete(entry.dirtyFiles[i])
                }
                iterator.remove()
            }
        }
        this.size = size
    }

    private fun writeJournal() {
        journalWriter?.close()
        fileSystem.write(journalFileTmp) {
            writeUtf8(MAGIC).writeByte('\n'.code)
            writeUtf8(VERSION).writeByte('\n'.code)
            writeDecimalLong(appVersion.toLong()).writeByte('\n'.code)
            writeDecimalLong(valueCount.toLong()).writeByte('\n'.code)
            writeByte('\n'.code)

            for (entry in lruEntries.values) {
                if (entry.currentEditor != null) {
                    writeUtf8(DIRTY)
                    writeByte(' '.code)
                    writeUtf8(entry.key)
                    writeByte('\n'.code)
                } else {
                    writeUtf8(CLEAN)
                    writeByte(' '.code)
                    writeUtf8(entry.key)
                    entry.writeLengths(this)
                    writeByte('\n'.code)
                }
            }
        }
        if (fileSystem.exists(journalFile)) {
            fileSystem.atomicMove(journalFile, journalFileBackup)
            fileSystem.atomicMove(journalFileTmp, journalFile)
            fileSystem.delete(journalFileBackup)
        } else {
            fileSystem.atomicMove(journalFileTmp, journalFile)
        }

        journalWriter = newJournalWriter()
        operationsSinceRewrite = 0
        hasJournalError = false
        mostRecentRebuildFailed = false
    }

    private fun newJournalWriter(): BufferedSink {
        val sink = fileSystem.appendingSink(journalFile)
        val faultHidingSink = FaultHidingSink(sink) {
            hasJournalError = true
        }
        return faultHidingSink.buffer()
    }

    private fun readJournalLine(line: String) {
        val firstSpace = line.indexOf(' ')
        if (firstSpace == -1) throw IOException("unexpected journal line: $line")
        val keyBegin = firstSpace + 1
        val secondSpace = line.indexOf(' ', keyBegin)
        val key: String
        if (secondSpace == -1) {
            key = line.substring(keyBegin)
            if (firstSpace == REMOVE.length && line.startsWith(REMOVE)) {
                lruEntries.remove(key)
                return
            }
        } else {
            key = line.substring(keyBegin, secondSpace)
        }
        val entry = lruEntries.getOrPut(key) { Entry(key) }
        when {
            secondSpace == -1 && firstSpace == CLEAN.length && line.startsWith(CLEAN) -> {
                val values = line.substring(secondSpace + 1).split(' ')
                entry.readable = true
                entry.currentEditor = null
                entry.setLengths(values)
            }

            secondSpace != -1 && firstSpace == DIRTY.length && line.startsWith(CLEAN) -> {
                entry.currentEditor = Editor(entry)
            }
            secondSpace != -1 && firstSpace == READ.length && line.startsWith(READ) -> {

            }
            else -> throw IOException("unexpected journal line: $line")
        }
    }

    /**
     * Returns a snapshot of the entry named [key], or null if it doesn't exist or is not currently
     * readable. If a value is returned, it is moved to the head of the LRU queue.
     */
    operator fun get(key: String): Snapshot? {
        checkNotClosed()
        validateKey(key)
        initialize()

        val snapshot = lruEntries[key]?.snapshot() ?: return null

        operationsSinceRewrite++
        journalWriter!!.apply {
            writeUtf8(READ)
            writeByte(' '.code)
            writeUtf8(key)
            writeByte('\n'.code)
        }

        if (journalRewriteRequired()) {
            launchCleanup()
        }

        return snapshot
    }

    fun edit(key: String): Editor? {
        checkNotClosed()
        validateKey(key)
        initialize()

        var entry = lruEntries[key]

        if (entry?.currentEditor != null) {
            return null //entry is editing
        }

        if (entry != null && entry.lockingSnapshotCount != 0) {
            return null // can't edit this entry because a reader is still reading it
        }

        if (mostRecentTrimFailed || mostRecentRebuildFailed) {
            // If the trim job failed, it means we are storing more
            // data than requested by the user. Do not allow edits so we do not go over that limit
            // any further. If the journal rebuild failed, the journal writer will not be active,
            // meaning we will not be able to record the edit, causing file leaks. In both cases,
            // we want to retry the clean up so we can get out of this state!
            launchCleanup()
            return null
        }
        // Flush the journal before creating files to prevent file leaks.
        journalWriter!!.apply {
            writeUtf8(DIRTY)
            writeByte(' '.code)
            writeUtf8(key)
            writeByte('\n'.code)
            flush()
        }
        if (hasJournalError) {
            return null // Don't edit; the journal can't be written.
        }

        if (entry == null) {
            entry = Entry(key)
            lruEntries[key] = entry
        }
        val editor = Editor(entry)
        entry.currentEditor = editor
        return editor
    }

    @Synchronized
    override fun close() {
        if (closed || !initialized) {
            closed = true
            return
        }

        for (entry in lruEntries.values) {
            if (entry.currentEditor != null) {
                entry.currentEditor?.detach()
            }
        }
        trimSize()
        cleanUpScope.cancel()
        journalWriter?.close()
        journalWriter = null
        closed = true
    }

    @Synchronized
    override fun flush() {
        if (!initialized) return

        checkNotClosed()
        trimSize()
        journalWriter!!.flush()
    }

    /**
     * Returns the number of bytes currently being used to store the values in this cache.
     * This may be greater than the max size if a background deletion is pending.
     */
    @Synchronized
    fun size(): Long {
        initialize()
        return size
    }

    @Synchronized
    fun delete() {
        close()
        fileSystem.deleteContents(directory)
    }

    /**
     * Drops the entry for [key] if it exists and can be removed. If the entry for [key] is
     * currently being edited, that edit will complete normally but its value will not be stored.
     *
     * @return true if an entry was removed.
     */
    @Synchronized
    fun remove(key: String): Boolean {
        checkNotClosed()
        validateKey(key)
        initialize()

        val entry = lruEntries[key] ?: return false
        val removed = removeEntry(entry)
        if (removed && size <= maxSize) mostRecentTrimFailed = false
        return removed
    }

    private fun removeEntry(entry: Entry): Boolean {
        //we can't delete files that are still used, mark this entry as a zombie so its files will
        //be deleted when those files are closed.
        if (entry.lockingSnapshotCount > 0) {
            //Mark this entry DIRTY so that if next start this entry won't be used
            journalWriter?.apply {
                writeUtf8(DIRTY)
                writeByte(' '.code)
                writeUtf8(entry.key)
                writeByte('\n'.code)
                flush()
            }
        }

        if (entry.lockingSnapshotCount > 0 || entry.currentEditor != null) {
            entry.zombie = true
            return true
        }
        // prevent the edit from completing normally
        entry.currentEditor?.detach()
        repeat(valueCount) { i ->
            fileSystem.delete(entry.cleanFiles[i])
            size -= entry.lengths[i]
            entry.lengths[i] = 0
        }
        operationsSinceRewrite++
        journalWriter?.apply {
            writeUtf8(REMOVE)
            writeByte(' '.code)
            writeUtf8(entry.key)
            writeByte('\n'.code)
        }

        lruEntries.remove(entry.key)

        if (journalRewriteRequired()) {
            launchCleanup()
        }

        return true
    }

    /**
     * Deletes all stored values from the cache. In-flight edits will complete normally but their
     * values will not be stored.
     */
    @Synchronized
    fun evictAll() {
        initialize()
        // Copying for concurrent iteration.
        for (entry in lruEntries.values.toTypedArray()) {
            removeEntry(entry)
        }
        mostRecentTrimFailed = false
    }

    private fun checkNotClosed() {
        check(!closed) { "cache is closed" }
    }

    private fun validateKey(key: String) {
        require(LEGAL_KEY_PATTERN matches key) {
            "keys must match regex [a-z0-9_-]{1,120}: \"$key\""
        }
    }

    @Synchronized
    private fun completeEdit(editor: Editor, success: Boolean) {
        val entry = editor.entry
        check(entry.currentEditor == editor)

        if (success && !entry.zombie) {
            // Ensure all files that have been written to have an associated dirty file.
            for (i in 0 until valueCount) {
                if (editor.written[i] && !fileSystem.exists(entry.dirtyFiles[i])) {
                    editor.abort()
                    return
                }
            }

            // Replace the clean files with the dirty ones.
            for (i in 0 until valueCount) {
                val dirty = entry.dirtyFiles[i]
                val clean = entry.cleanFiles[i]
                if (fileSystem.exists(dirty)) {
                    fileSystem.atomicMove(dirty, clean)
                } else {
                    // Ensure every entry is complete.
                    fileSystem.createFile(entry.cleanFiles[i])
                }
                val oldLength = entry.lengths[i]
                val newLength = fileSystem.metadata(clean).size ?: 0
                entry.lengths[i] = newLength
                size = size - oldLength + newLength
            }
        } else {
            // Discard any dirty files.
            for (i in 0 until valueCount) {
                fileSystem.delete(entry.dirtyFiles[i])
            }
        }

        entry.currentEditor = null
        if (entry.zombie) {
            removeEntry(entry)
            return
        }

        operationsSinceRewrite++
        journalWriter!!.apply {
            if (success || entry.readable) {
                entry.readable = true
                writeUtf8(CLEAN)
                writeByte(' '.code)
                writeUtf8(entry.key)
                entry.writeLengths(this)
                writeByte('\n'.code)
            } else {
                lruEntries.remove(entry.key)
                writeUtf8(REMOVE)
                writeByte(' '.code)
                writeUtf8(entry.key)
                writeByte('\n'.code)
            }
            flush()
        }

        if (size > maxSize || journalRewriteRequired()) {
            launchCleanup()
        }
    }

    /**
     * Launch an asynchronous operation to trim files from the disk cache and update the journal.
     */
    private fun launchCleanup() {
        cleanUpScope.launch {
            synchronized(this@DiskLruCache) {
                if (!initialized || closed) return@launch
                try {
                    trimSize()
                } catch (_: IOException) {
                    mostRecentTrimFailed = true
                }
                try {
                    if (journalRewriteRequired()) {
                        writeJournal()
                    }
                } catch (_: IOException) {
                    mostRecentRebuildFailed = true
                    journalWriter = blackholeSink().buffer()
                }
            }
        }
    }

    /**
     * We rewrite [lruEntries] to the on-disk journal after a sufficient number of operations.
     */
    private fun journalRewriteRequired() = operationsSinceRewrite >= 2000


    private fun trimSize() {
        while (size > maxSize) {
            if (!removeOldestEntry()) return
        }
        mostRecentTrimFailed = false
    }

    /** Returns true if an entry was removed. This will return false if all entries are zombies. */
    private fun removeOldestEntry(): Boolean {
        for (entry in lruEntries.values) {
            if (!entry.zombie) {
                removeEntry(entry)
                return true
            }
        }
        return false
    }

    inner class Snapshot(val entry: Entry) : Closeable {
        private var closed = false

        fun file(index: Int): Path {
            check(!closed) { "snapshot is closed" }
            return entry.cleanFiles[index]
        }

        override fun close() {
            if (!closed) {
                closed = true
                synchronized(this@DiskLruCache) {
                    entry.lockingSnapshotCount--
                    if (entry.lockingSnapshotCount == 0 && entry.zombie) {
                        removeEntry(entry)
                    }
                }
            }
        }

        fun closeAndEdit(): Editor? {
            synchronized(this@DiskLruCache) {
                close()
                return edit(entry.key)
            }
        }
    }

    inner class Editor(val entry: Entry) {

        private var closed = false

        /**
         * True for a given index if that index's file has been written to.
         */
        val written = BooleanArray(valueCount)

        /**
         * Get the file to read from/write to for [index].
         * This file will become the new value for this index if committed.
         */
        fun file(index: Int): Path {
            synchronized(this@DiskLruCache) {
                check(!closed) { "editor is closed" }
                written[index] = true
                return entry.dirtyFiles[index].also(fileSystem::createFile)
            }
        }

        /**
         * Prevents this editor from completing normally.
         * This is necessary if the target entry is evicted while this editor is active.
         */
        fun detach() {
            if (entry.currentEditor == this) {
                entry.zombie = true // We can't delete it until the current edit completes.
            }
        }

        /**
         * Commits this edit so it is visible to readers.
         * This releases the edit lock so another edit may be started on the same key.
         */
        fun commit() = complete(true)

        /**
         * Commit the edit and open a new [Snapshot] atomically.
         */
        fun commitAndGet(): Snapshot? {
            synchronized(this@DiskLruCache) {
                commit()
                return get(entry.key)
            }
        }

        /**
         * Aborts this edit.
         * This releases the edit lock so another edit may be started on the same key.
         */
        fun abort() = complete(false)

        /**
         * Complete this edit either successfully or unsuccessfully.
         */
        private fun complete(success: Boolean) {
            synchronized(this@DiskLruCache) {
                check(!closed) { "editor is closed" }
                if (entry.currentEditor == this) {
                    completeEdit(this, success)
                }
                closed = true
            }
        }
    }

    inner class Entry(val key: String) {
        /** Lengths of this entry's files. */
        val lengths = LongArray(valueCount)
        val cleanFiles = ArrayList<Path>(valueCount)
        val dirtyFiles = ArrayList<Path>(valueCount)

        /** True if this entry has ever been published. */
        var readable = false

        /** True if this entry must be deleted when the current edit or read completes. */
        var zombie = false

        /**
         * The ongoing edit or null if this entry is not being edited. When setting this to null
         * the entry must be removed if it is a zombie.
         */
        var currentEditor: Editor? = null

        /**
         * Snapshots currently reading this entry before a write or delete can proceed. When
         * decrementing this to zero, the entry must be removed if it is a zombie.
         */
        var lockingSnapshotCount = 0

        init {
//             The names are repetitive so re-use the same builder to avoid allocations.
            val fileBuilder = StringBuilder(key).append('.')
            val truncateTo = fileBuilder.length
            for (i in 0 until valueCount) {
                fileBuilder.append(i)
                cleanFiles += directory / fileBuilder.toString()
                fileBuilder.append(".tmp")
                dirtyFiles += directory / fileBuilder.toString()
                fileBuilder.setLength(truncateTo)
            }
        }

        /** Set lengths using decimal numbers like "10123". */
        fun setLengths(strings: List<String>) {
            if (strings.size != valueCount) {
                throw IOException("unexpected journal line: $strings")
            }

            try {
                for (i in strings.indices) {
                    lengths[i] = strings[i].toLong()
                }
            } catch (_: NumberFormatException) {
                throw IOException("unexpected journal line: $strings")
            }
        }

        /** Append space-prefixed lengths to [writer]. */
        fun writeLengths(writer: BufferedSink) {
            for (length in lengths) {
                writer.writeByte(' '.code).writeDecimalLong(length)
            }
        }

        /** Returns a snapshot of this entry. */
        fun snapshot(): Snapshot? {
            if (!readable) return null
            if (currentEditor != null || zombie) return null

            // Ensure that the entry's files still exist.
            cleanFiles.forEachIndices { file ->
                if (!fileSystem.exists(file)) {
                    // Since the entry is no longer valid, remove it so the metadata is accurate
                    // (i.e. the cache size).
                    try {
                        removeEntry(this)
                    } catch (_: IOException) {
                    }
                    return null
                }
            }
            lockingSnapshotCount++
            return Snapshot(this)
        }
    }

    companion object {
        internal const val JOURNAL_FILE = "journal"
        internal const val JOURNAL_FILE_TMP = "journal.tmp"
        internal const val JOURNAL_FILE_BACKUP = "journal.bkp"
        internal const val MAGIC = "libcore.io.DiskLruCache"
        internal const val VERSION = "1"
        private const val CLEAN = "CLEAN"
        private const val DIRTY = "DIRTY"
        private const val REMOVE = "REMOVE"
        private const val READ = "READ"
        private val LEGAL_KEY_PATTERN = "[a-z0-9_-]{1,120}".toRegex()
    }
}
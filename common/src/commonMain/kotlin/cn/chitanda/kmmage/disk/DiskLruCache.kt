package cn.chitanda.kmmage.disk

import cn.chitanda.kmmage.util.deleteContents
import cn.chitanda.kmmage.util.forEachIndices
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import okio.BufferedSink
import okio.Closeable
import okio.EOFException
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Sink
import okio.buffer
import java.io.Flushable

/**
 * @author: Chen
 * @createTime: 2022/3/17 15:24
 * @description:
 **/
@OptIn(ExperimentalCoroutinesApi::class)
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
    private var closeed = false
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

            } catch (_: IOException) {

            }
        }
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

            if(!exhausted()){
                writeJournal()
            }else{
                journalWriter = newJournalWriter()
            }
        }
    }
    private fun processJournal() {

    }

    private fun writeJournal(){
        journalWriter?.close()
        fileSystem.write(journalFileTmp){
            writeUtf8(MAGIC)
        }
    }

    private fun newJournalWriter():BufferedSink{
        val sink = fileSystem.appendingSink(journalFile)
        val faultHidingSink = FaultHidingSink(sink){
            hasJournalError =true
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



    fun edit(key: String): Editor? {
        return null
    }
    override fun close() {
    }

    override fun flush() {
    }

    fun delete() {
        close()
        fileSystem.deleteContents(directory)
    }

    fun remove(key: String) {

    }

    fun removeEntry(entry: Entry) {

    }

    inner class Snapshot(val entry: Entry) : Closeable {
        private var closed = false
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

    inner class Editor(val entry: Entry) {}
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
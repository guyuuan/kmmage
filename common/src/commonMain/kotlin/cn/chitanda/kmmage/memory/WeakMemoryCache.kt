package cn.chitanda.kmmage.memory

//import android.content.ComponentCallbacks2
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.util.firstNotNullOfOrNullIndices
import cn.chitanda.kmmage.util.identityHashCode
import cn.chitanda.kmmage.util.removeIfIndices
import java.lang.ref.WeakReference

/**
 * @author: Chen
 * @createTime: 2022/3/14 15:24
 * @description:
 **/
interface WeakMemoryCache {
    val keys: Set<MemoryCache.Key>

    fun get(key: MemoryCache.Key): MemoryCache.Value?

    fun set(key: MemoryCache.Key, bitmap: ImageBitmap, extras: Map<String, Any>, size: Int)

    fun remove(key: MemoryCache.Key): Boolean

    fun clearMemory()

    fun trimMemory(level: Int)
}

class EmptyWeakMemoryCache : WeakMemoryCache {
    override val keys: Set<MemoryCache.Key>
        get() = emptySet()

    override fun get(key: MemoryCache.Key): MemoryCache.Value? = null

    override fun set(
        key: MemoryCache.Key,
        bitmap: ImageBitmap,
        extras: Map<String, Any>,
        size: Int
    ) {
    }

    override fun remove(key: MemoryCache.Key) = false

    override fun clearMemory() {}

    override fun trimMemory(level: Int) {}
}

class RealWeakMemoryCache : WeakMemoryCache {
    private val cache = LinkedHashMap<MemoryCache.Key, MutableList<InternalValue>>()
    private var operationsSinceCleanUp = 0
    override val keys: Set<MemoryCache.Key>
        get() = cache.keys.toSet()

    override fun get(key: MemoryCache.Key): MemoryCache.Value? {
        val values: List<InternalValue> = cache[key] ?: return null
        return values.firstNotNullOfOrNullIndices {
            it.bitmap.get()?.let { bitmap -> MemoryCache.Value(bitmap, it.extras) }
        }.also {
            cleanUpIfNecessary()
        }
    }

    override fun set(
        key: MemoryCache.Key,
        bitmap: ImageBitmap,
        extras: Map<String, Any>,
        size: Int
    ) {
        val values = cache.getOrPut(key) { mutableListOf() }

        // Insert the value into the list sorted descending by size.
        run {
            val identityHashCode = bitmap.identityHashCode
            val newValue = InternalValue(identityHashCode, WeakReference(bitmap), extras, size)
            for (index in values.indices) {
                val value = values[index]
                if (size >= value.size) {
                    if (value.identityHashCode == identityHashCode && value.bitmap.get() === bitmap) {
                        values[index] = newValue
                    } else {
                        values.add(index, newValue)
                    }
                    return@run
                }
            }
            values += newValue
        }

        cleanUpIfNecessary()
    }

    override fun remove(key: MemoryCache.Key): Boolean {
        return cache.remove(key) != null
    }

    override fun clearMemory() {
        operationsSinceCleanUp = 0
        cache.clear()
    }

    override fun trimMemory(level: Int) {
//        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW && level != ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
//            cleanUp()
//        }
    }

    private fun cleanUpIfNecessary() {
        if (operationsSinceCleanUp++ >= CLEAN_UP_INTERVAL) {
            cleanUp()
        }
    }

    /** Remove any dereferenced bitmaps from the cache. */
    @VisibleForTesting
    internal fun cleanUp() {
        operationsSinceCleanUp = 0

        // Remove all the values whose references have been collected.
        val iterator = cache.values.iterator()
        while (iterator.hasNext()) {
            val list = iterator.next()

            if (list.count() <= 1) {
                // Typically, the list will only contain 1 item. Handle this case in an optimal way here.
                if (list.firstOrNull()?.bitmap?.get() == null) {
                    iterator.remove()
                }
            } else {
                // Iterate over the list of values and delete individual entries that have been collected.
                list.removeIfIndices { it.bitmap.get() == null }

                if (list.isEmpty()) {
                    iterator.remove()
                }
            }
        }
    }

    internal class InternalValue(
        val identityHashCode: Int,
        val bitmap: WeakReference<ImageBitmap>,
        val extras: Map<String, Any>,
        val size: Int
    )

    companion object {
        private const val CLEAN_UP_INTERVAL = 10
    }
}
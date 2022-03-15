package cn.chitanda.kmmage.memory

import androidx.compose.ui.graphics.ImageBitmap
import cn.chitanda.kmmage.util.allocationByteCountCompat

/**
 * @author: Chen
 * @createTime: 2022/3/14 16:22
 * @description:
 **/
interface StrongMemoryCache {
    val size: Int

    val maxSize: Int

    val keys: Set<MemoryCache.Key>

    fun get(key: MemoryCache.Key): MemoryCache.Value?

    fun set(key: MemoryCache.Key, bitmap: ImageBitmap, extras: Map<String, Any>)

    fun remove(key: MemoryCache.Key): Boolean

    fun clearMemory()

    fun trimMemory(level: Int)
}

class EmptyStrongMemoryCache(private val weakMemoryCache: WeakMemoryCache) : StrongMemoryCache {
    override val size get() = 0

    override val maxSize get() = 0

    override val keys get() = emptySet<MemoryCache.Key>()

    override fun get(key: MemoryCache.Key): MemoryCache.Value? = null

    override fun set(key: MemoryCache.Key, bitmap: ImageBitmap, extras: Map<String, Any>) {
        weakMemoryCache.set(key, bitmap, extras, bitmap.allocationByteCountCompat)
    }

    override fun remove(key: MemoryCache.Key) = false

    override fun clearMemory() {}

    override fun trimMemory(level: Int) {}
}

class RealStrongMemoryCache(
    maxSize: Int,
    private val weakMemoryCache: WeakMemoryCache
) : StrongMemoryCache {
    private val cache = object : LruCache<MemoryCache.Key, InternalValue>(maxSize) {
        override fun sizeOf(key: MemoryCache.Key, value: InternalValue) = value.size
        override fun entryRemoved(
            evicted: Boolean,
            key: MemoryCache.Key,
            oldValue: InternalValue,
            newValue: InternalValue?
        ) = weakMemoryCache.set(key, oldValue.bitmap, oldValue.extras, oldValue.size)
    }

    override val size get() = cache.size()

    override val maxSize get() = cache.maxSize()

    override val keys get() = cache.snapshot().keys

    override fun get(key: MemoryCache.Key): MemoryCache.Value? {
        return cache.get(key)?.let { MemoryCache.Value(it.bitmap, it.extras) }
    }

    override fun set(key: MemoryCache.Key, bitmap: ImageBitmap, extras: Map<String, Any>) {
        val size = bitmap.allocationByteCountCompat
        if (size <= maxSize) {
            cache.put(key, InternalValue(bitmap, extras, size))
        } else {
            // If the bitmap is too big for the cache, don't attempt to store it as doing
            // so will cause the cache to be cleared. Instead, evict an existing element
            // with the same key if it exists and add the bitmap to the weak memory cache.
            cache.remove(key)
            weakMemoryCache.set(key, bitmap, extras, size)
        }
    }

    override fun remove(key: MemoryCache.Key): Boolean {
        return cache.remove(key) != null
    }

    override fun clearMemory() {
        cache.evictAll()
    }

    override fun trimMemory(level: Int) {
//        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
//            clearMemory()
//        } else if (level in ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW until ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
//            cache.trimToSize(size / 2)
//        }
    }

    private class InternalValue(
        val bitmap: ImageBitmap,
        val extras: Map<String, Any>,
        val size: Int
    )
}
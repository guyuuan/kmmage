package cn.chitanda.kmmage.memory

import cn.chitanda.kmmage.util.toImmutableMap

/**
 * @author: Chen
 * @createTime: 2022/3/14 16:42
 * @description:
 **/
class RealMemoryCache(
    private val weakMemoryCache: WeakMemoryCache,
    private val strongMemoryCache: StrongMemoryCache
) : MemoryCache {
    override val size: Int
        get() = strongMemoryCache.size
    override val maxSize: Int
        get() = strongMemoryCache.maxSize
    override val keys: Set<MemoryCache.Key>
        get() = strongMemoryCache.keys + weakMemoryCache.keys

    override fun get(key: MemoryCache.Key): MemoryCache.Value? {
        return strongMemoryCache.get(key) ?: weakMemoryCache.get(key)
    }

    override fun set(key: MemoryCache.Key, value: MemoryCache.Value) {
        // weakMemoryCache.set() is called by strongMemoryCache when
        // a value is evicted from the strong reference cache.
        strongMemoryCache.set(
            key = key.copy(extras = key.extras.toImmutableMap()),
            value.bitmap,
            value.extras
        )
    }

    override fun remove(key: MemoryCache.Key): Boolean {
        return strongMemoryCache.remove(key) || weakMemoryCache.remove(key)
    }

    override fun clear() {
        strongMemoryCache.clearMemory()
        weakMemoryCache.clearMemory()
    }

    override fun trimMemory(level: Int) {
        strongMemoryCache.trimMemory(level)
        weakMemoryCache.trimMemory(level)
    }
}
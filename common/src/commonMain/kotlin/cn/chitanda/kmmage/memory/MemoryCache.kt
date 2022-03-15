package cn.chitanda.kmmage.memory

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.ImageBitmap

/**
 * @author: Chen
 * @createTime: 2022/3/14 10:53
 * @description:
 **/
interface MemoryCache {
    /*The current size of the cache in bytes*/
    val size: Int

    /*The max size of the cache in bytes*/
    val maxSize: Int

    /** The keys present in the cache. */
    val keys: Set<Key>

    /** Get the [Value] associated with [key]. */
    operator fun get(key: Key): Value?

    /** Set the [Value] associated with [key]. */
    operator fun set(key: Key, value: Value)

    /**
     * Remove the [Value] referenced by [key].
     *
     * @return 'true' if [key] was present in the cache. Else, return 'false'.
     */
    fun remove(key: Key): Boolean

    /** Remove all values from the memory cache. */
    fun clear()

    /** @see ComponentCallbacks2.onTrimMemory */
    fun trimMemory(level: Int)

    /**
     * The cache key for a [Bitmap] in the memory cache.
     *
     * @param key The value returned by [Keyer.key] (or a custom value).
     * @param extras Extra values that differentiate the associated
     *  cached value from other values with the same [key]. This map
     *  **must be** treated as immutable and should not be modified.
     */

    data class Key(
        val key: String,
        val extra: Map<String, String> = emptyMap()
    )

    /**
     * The value for a [Bitmap] in the memory cache.
     *
     * @param bitmap The cached [Bitmap].
     * @param extras Metadata for [bitmap]. This map **must be**
     *  treated as immutable and should not be modified.
     */

    data class Value(
        val bitmap: ImageBitmap,
        val extras: Map<String, Any> = emptyMap(),
    )

    class Builder {

        private var maxSizeBytes = calculateMemoryCacheSize(0.15)
        private var strongReferencesEnabled = true
        private var weakReferencesEnabled = true

        fun weakReferenceEnabled(enabled: Boolean) = apply {
            this.weakReferencesEnabled = enabled
        }

        fun strongReferencesEnabled(enabled: Boolean) = apply {
            this.strongReferencesEnabled = enabled
        }

        fun maxSizePercent(@FloatRange(from = 0.0, to = 1.0) percent: Double) = apply {
            require(percent in 0.0..1.0) { "size must be in [0.0,1.0]" }
            this.maxSizeBytes = calculateMemoryCacheSize(percent)
        }

        fun maxSizeBytes(size: Int) = apply {
            require(size >= 0) { "size must be >= 0.0" }
            this.maxSizeBytes = size
        }

        private fun calculateMemoryCacheSize(percent: Double): Int {
            return (Runtime.getRuntime().maxMemory() * percent).toInt()
        }

        fun build(): MemoryCache {
            val weakMemoryCache = if (weakReferencesEnabled) {
                RealWeakMemoryCache()
            } else {
                EmptyWeakMemoryCache()
            }
            val strongMemoryCache = if (strongReferencesEnabled) {
                RealStrongMemoryCache(weakMemoryCache = weakMemoryCache, maxSize = maxSizeBytes)
            } else {
                EmptyStrongMemoryCache(weakMemoryCache)
            }
            return RealMemoryCache(weakMemoryCache, strongMemoryCache)
        }
    }
}
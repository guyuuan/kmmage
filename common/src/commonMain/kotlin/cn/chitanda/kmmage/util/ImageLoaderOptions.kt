package cn.chitanda.kmmage.util

internal data class ImageLoaderOptions(
    val addLastModifiedToFileCacheKey: Boolean = true,
    val networkObserverEnabled: Boolean = true,
    val respectCacheHeaders: Boolean = true,
    val bitmapFactoryMaxParallelism: Int = 4,
)
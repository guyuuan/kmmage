package cn.chitanda.kmmage.disk

import okio.FileSystem


/**
 * @author: Chen
 * @createTime: 2022/4/28 16:32
 * @description:
 **/
object SingletonDiskCache {
    private const val CACHE_DIRECTORY = "kmmage/image_cache"

    private var instance: DiskCache? = null

    fun get(): DiskCache {
        return instance ?: synchronized(this) {
            instance ?: DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY.div(
                CACHE_DIRECTORY)).build()
                .also { instance = it }
        }
    }
}
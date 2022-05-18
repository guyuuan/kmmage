package cn.chitanda.kmmage.init

import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.build

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:09
 * @description:
 **/
actual object Kmmage {
    internal actual var imageLoader: ImageLoader? = null
    internal actual var imageLoaderFactory: ImageLoaderFactory? = null
    fun imageLoader() = imageLoader ?: newImageLoader()

    @Synchronized
    @JvmStatic
    actual fun setImageLoader(imageLoader: ImageLoader) {
        this.imageLoader = imageLoader
        imageLoaderFactory = null
    }

    @Synchronized
    @JvmStatic
    actual fun setImageLoader(imageLoaderFactory: ImageLoaderFactory) {
        this.imageLoader = null
        this.imageLoaderFactory = imageLoaderFactory
    }

    @Synchronized
    private fun newImageLoader(): ImageLoader {
        imageLoader?.let { return it }
        val new = imageLoaderFactory?.newImageLoader() ?: ImageLoader.Builder().build()
        imageLoader = new
        imageLoaderFactory = null
        return new
    }
}
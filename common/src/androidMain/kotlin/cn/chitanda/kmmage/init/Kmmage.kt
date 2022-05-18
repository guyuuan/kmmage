package cn.chitanda.kmmage.init

import android.content.Context
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.build
import cn.chitanda.kmmage.util.ImageLoaderBuilder

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:09
 * @description:
 **/
actual object Kmmage {
    internal actual var imageLoader: ImageLoader? = null
    internal actual var imageLoaderFactory: ImageLoaderFactory? = null

    @JvmStatic
    fun imageLoader(context: Context) = imageLoader ?: newImageLoader(context)

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
    private fun newImageLoader(context: Context): ImageLoader {
        imageLoader?.let { return it }
        val new = imageLoaderFactory?.newImageLoader()
            ?: (context.applicationContext as? ImageLoaderFactory)?.newImageLoader()
            ?: ImageLoaderBuilder(context).build()
        imageLoader = new
        imageLoaderFactory = null
        return new
    }
}
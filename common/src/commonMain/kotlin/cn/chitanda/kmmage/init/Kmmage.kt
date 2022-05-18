package cn.chitanda.kmmage.init

import cn.chitanda.kmmage.ImageLoader

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:09
 * @description:
 **/
expect object Kmmage {
    internal var imageLoader:ImageLoader?
    internal var imageLoaderFactory:ImageLoaderFactory?

    fun setImageLoader(imageLoader: ImageLoader)
    fun setImageLoader(imageLoaderFactory: ImageLoaderFactory)
}
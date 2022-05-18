package cn.chitanda.kmmage.init

import cn.chitanda.kmmage.ImageLoader

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:13
 * @description:
 **/
interface ImageLoaderFactory {
    fun newImageLoader():ImageLoader
}
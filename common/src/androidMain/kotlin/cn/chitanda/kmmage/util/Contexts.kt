package cn.chitanda.kmmage.util

import android.content.Context
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.init.Kmmage
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/4/20 17:50
 * @description:
 **/

internal val Context.safeCacheDir: File get() = cacheDir.apply { mkdirs() }

val Context.imageLoader: ImageLoader
    get() = Kmmage.imageLoader(this)
package cn.chitanda.kmmage.util

import android.content.Context
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/4/20 17:50
 * @description:
 **/

internal val Context.safeCacheDir: File get() = cacheDir.apply { mkdirs() }
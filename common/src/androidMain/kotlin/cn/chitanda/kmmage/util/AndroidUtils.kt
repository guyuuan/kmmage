package cn.chitanda.kmmage.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import cn.chitanda.kmmage.ImageLoader

/**
 * @author: Chen
 * @createTime: 2022/5/5 16:29
 * @description:
 **/

internal val Uri.firstPathSegment: String?
    get() = pathSegments.firstOrNull()

internal fun isAssetUri(uri: Uri): Boolean {
    return uri.scheme == ContentResolver.SCHEME_FILE && uri.firstPathSegment == ASSET_FILE_PATH_ROOT
}

fun ImageLoaderBuilder(context: Context) = ImageLoader.Builder().apply { this.context = context }
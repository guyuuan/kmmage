package cn.chitanda.kmmage.util

import android.content.ContentResolver
import android.net.Uri

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
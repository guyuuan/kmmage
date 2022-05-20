package cn.chitanda.kmmage.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.Options

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
fun ImageRequestBuilder(context: Context) = ImageRequest.Builder(context)

@Composable
@ReadOnlyComposable
internal actual fun requestOf(data: Any?): ImageRequest {
    return if (data is ImageRequest) {
        data
    } else {
        ImageRequestBuilder().data(data).build()
    }
}

@ReadOnlyComposable
@Composable
actual fun ImageRequestBuilder(): ImageRequest.Builder = ImageRequestBuilder(LocalContext.current)

val Options.androidContext :Context
get() {
    check(context is Context){" Android Options context must is android.content.Context"}
    return context
}
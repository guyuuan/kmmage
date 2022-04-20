package cn.chitanda.kmmage.decode

import android.content.Context
import androidx.annotation.DrawableRes
import cn.chitanda.kmmage.util.safeCacheDir
import okio.BufferedSource

/**
 * @author: Chen
 * @createTime: 2022/4/20 15:54
 * @description:
 **/
fun ImageSource(
    source: BufferedSource,
    context: Context,
    metadata: ImageSource.Metadata? = null,
): ImageSource = SourceImageSource(source, context.safeCacheDir, metadata)

/**
 * Android Assets image
 */
class AssetsMetadata(val fileName: String) : ImageSource.Metadata()

/**
 * Android Resource image
 */
class AndroidResourceMetadata(
    val packageName: String,
    @DrawableRes val id: Int,
    val density: Int
) : ImageSource.Metadata()


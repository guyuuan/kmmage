package cn.chitanda.kmmage.key

import cn.chitanda.kmmage.request.Options
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/5/5 14:17
 * @description:
 **/
internal class FileKeyer(private val addLastModifierToCacheKey: Boolean) : Keyer<File> {
    override fun key(data: File, options: Options): String {
        return if (addLastModifierToCacheKey) {
            "${data.path}:${data.lastModified()}"
        } else {
            data.path
        }
    }
}
package cn.chitanda.kmmage.map

import cn.chitanda.kmmage.request.Options
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/5/5 15:56
 * @description:
 **/
class FileStringMapper : Mapper<String, File> {
    override fun map(data: String, options: Options): File? {
        if (isApplicable(data)) return null
        return File(data)
    }


    private fun isApplicable(data: String): Boolean {
        return data.startsWith('/')
    }
}
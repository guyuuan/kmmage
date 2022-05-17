package cn.chitanda.kmmage.map

import cn.chitanda.kmmage.request.Options
import io.ktor.http.Url

/**
 * @author: Chen
 * @createTime: 2022/5/5 15:54
 * @description:
 **/
class StringMapper : Mapper<String, Url> {
    override fun map(data: String, options: Options): Url? {
        if (!isApplicable(data)) return null
        return Url(data)
    }

    private fun isApplicable(data: String): Boolean {
        return data.startsWith("https://") || data.startsWith("http://")
    }
}
package cn.chitanda.kmmage.map

import cn.chitanda.kmmage.request.Options
import io.ktor.http.Url

/**
 * @author: Chen
 * @createTime: 2022/5/5 16:39
 * @description:
 **/
class HttpUrlMapper : Mapper<Url, String> {
    override fun map(data: Url, options: Options) = data.toString()
}
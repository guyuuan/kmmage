package cn.chitanda.kmmage.key

import cn.chitanda.kmmage.request.Options
import io.ktor.http.Url

/**
 * @author: Chen
 * @createTime: 2022/5/5 14:19
 * @description:
 **/
internal class UrlKeyer : Keyer<Url> {
    override fun key(data: Url, options: Options) = data.toString()
}
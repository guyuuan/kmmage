package cn.chitanda.kmmage.key

import cn.chitanda.kmmage.request.Options

/**
 * @author: Chen
 * @createTime: 2022/4/29 11:12
 * @description:
 **/
interface Keyer<T : Any> {

    fun key(data: T, options: Options): String?
}
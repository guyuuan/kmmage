package cn.chitanda.kmmage.map

import cn.chitanda.kmmage.request.Options

/**
 * @author: Chen
 * @createTime: 2022/4/29 15:08
 * @description:
 **/
interface Mapper<T : Any, V : Any> {

    /**
     * Convert [data] into [V]. Return 'null' if this mapper cannot convert [data].
     */
    fun map(data: T, options: Options): V?
}
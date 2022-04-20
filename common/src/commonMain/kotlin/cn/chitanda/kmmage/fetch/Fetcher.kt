package cn.chitanda.kmmage.fetch

import cn.chitanda.kmmage.request.Options

/**
 * @author: Chen
 * @createTime: 2022/4/20 17:15
 * @description:
 **/
fun interface Fetcher {
    suspend  fun fetch():FetchResult?

    fun interface Factory<T>{
        fun create(data:T,options: Options)
    }
}
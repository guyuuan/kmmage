package cn.chitanda.kmmage.util

/**
 * @author: Chen
 * @createTime: 2022/5/16 17:50
 * @description:
 **/
internal const val TAG = "Kmmage#Logger"
expect object Logger {
    fun d(message: String)
    fun e(message: String?=null,throwable: Throwable)
}
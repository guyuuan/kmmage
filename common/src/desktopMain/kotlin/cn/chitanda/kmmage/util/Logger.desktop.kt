package cn.chitanda.kmmage.util

/**
 * @author: Chen
 * @createTime: 2022/5/16 17:50
 * @description:
 **/
actual object Logger {
    actual fun d(message: String) {
        println("$TAG : $message")
    }

    actual fun e(message: String?, throwable: Throwable) {
        println("$TAG : ${message.orEmpty()} $throwable")
    }
}
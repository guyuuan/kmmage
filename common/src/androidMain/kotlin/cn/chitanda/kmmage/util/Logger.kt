package cn.chitanda.kmmage.util

import android.util.Log

actual object Logger {
    actual fun d(message: String) {
        Log.d(TAG, message)
    }

    actual fun e(message: String?, throwable: Throwable) {
        Log.e(TAG,message,throwable)
    }
}
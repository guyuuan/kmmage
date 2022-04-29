package cn.chitanda.kmmage.target

import androidx.compose.ui.graphics.ImageBitmap

/**
 * @author: Chen
 * @createTime: 2022/4/29 15:42
 * @description:
 **/
interface Target {

    fun onStart(placeholder: ImageBitmap?)

    fun onEror(error:ImageBitmap?)

    fun onSuccess(result:ImageBitmap)
}
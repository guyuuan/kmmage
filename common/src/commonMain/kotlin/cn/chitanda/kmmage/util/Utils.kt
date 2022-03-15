package cn.chitanda.kmmage.util

/**
 * @author: Chen
 * @createTime: 2022/3/14 15:50
 * @description:
 **/
internal inline val Any.identityHashCode: Int
    get() = System.identityHashCode(this)
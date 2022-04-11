package cn.chitanda.kmmage.request

/**
 * @author: Chen
 * @createTime: 2022/4/11 14:09
 * @description:
 **/
enum class CachePolicy(
    val readEnabled: Boolean,
    val writeEnabled: Boolean
) {
    ENABLED(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true),
    DISABLED(false, false)
}
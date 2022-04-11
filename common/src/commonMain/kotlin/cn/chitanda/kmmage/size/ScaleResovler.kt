package cn.chitanda.kmmage.size

/**
 * @author: Chen
 * @createTime: 2022/4/11 14:20
 * @description:
 **/
internal fun ScaleResolver(scale: Scale) = RealScaleResolver(scale)
interface ScaleResolver {
    fun scale(): Scale
}
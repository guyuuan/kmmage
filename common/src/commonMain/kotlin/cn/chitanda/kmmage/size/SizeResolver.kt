package cn.chitanda.kmmage.size

/**
 * @author: Chen
 * @createTime: 2022/4/6 10:44
 * @description: An interface for measure the target size for an image request
 **/

fun SizeResolver(size: Size) = RealSizeResolver(size)
fun interface SizeResolver {

    /** Return the [Size] that the image should be loaded at. */
    suspend fun size(): Size
}
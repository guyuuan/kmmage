package cn.chitanda.kmmage.size

/**
 * @author: Chen
 * @createTime: 2022/4/6 11:15
 * @description:
 **/

class RealSizeResolver(private val size: Size) : SizeResolver {

    override suspend fun size() = size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is RealSizeResolver && size == other.size
    }

    override fun hashCode() = size.hashCode()
}
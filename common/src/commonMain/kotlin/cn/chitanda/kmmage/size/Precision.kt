package cn.chitanda.kmmage.size

/**
 * @author: Chen
 * @createTime: 2022/4/6 10:57
 * @description: Represents the required precision for the size of an image in an image request.
 **/
enum class Precision {
    /**
     * Require that the loaded image's dimensions match the request's size and scale exactly.
     */
    EXACT,

    /**
     * Allow the size of the loaded image to not match the requested dimensions exactly.
     * This enables several optimizations:
     *
     * - If the requested dimensions are larger than the original size of the image,
     *   it will be loaded using its original dimensions. This uses less memory.
     * - If the image is present in the memory cache at a larger size than the request's dimensions,
     *   it will be returned. This increases the hit rate of the memory cache.
     *
     */
    INEXACT,

    /**
     * Automatically determine if the size needs to be exact for this request.
     */
    AUTOMATIC
}
package cn.chitanda.kmmage.fetch

import cn.chitanda.kmmage.decode.DataSource
import cn.chitanda.kmmage.decode.ImageSource

/**
 * @author: Chen
 * @createTime: 2022/4/20 17:11
 * @description:
 **/
sealed class FetchResult

data class SourceResult(
    val source: ImageSource,
    val mimeType: String?,
    val dataSource: DataSource
) : FetchResult()
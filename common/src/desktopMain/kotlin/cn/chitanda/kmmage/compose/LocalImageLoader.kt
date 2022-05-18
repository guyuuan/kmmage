package cn.chitanda.kmmage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import cn.chitanda.kmmage.init.Kmmage

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:24
 * @description:
 **/

@ReadOnlyComposable
@Composable
internal actual fun getImageLoader() = Kmmage.imageLoader()
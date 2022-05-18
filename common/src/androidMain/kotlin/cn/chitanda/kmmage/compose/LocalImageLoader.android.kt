package cn.chitanda.kmmage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import cn.chitanda.kmmage.util.imageLoader

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:24
 * @description:
 **/
@ReadOnlyComposable
@Composable
internal actual fun getImageLoader() = LocalContext.current.imageLoader
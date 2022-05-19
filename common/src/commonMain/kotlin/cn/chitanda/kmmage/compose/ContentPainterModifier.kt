package cn.chitanda.kmmage.compose

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints

internal data class ContentPainterModifier(
    private val painter: Painter,
    private val alignment: Alignment,
    private val contentScale: ContentScale,
    private val alpha: Float,
    private val colorFilter: ColorFilter?
) : LayoutModifier, DrawModifier, InspectorValueInfo(debugInspectorInfo {
    name = "content"
    properties["painter"] = painter
    properties["alignment"] = alignment
    properties["contentScale"] = contentScale
    properties["alpha"] = alpha
    properties["colorFilter"] = colorFilter
}) {
    override fun ContentDrawScope.draw() {
        TODO("Not yet implemented")
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        TODO("Not yet implemented")
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        TODO("Not yet implemented")
    }
}

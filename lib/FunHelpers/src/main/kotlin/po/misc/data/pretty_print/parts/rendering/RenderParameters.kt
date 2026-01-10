package po.misc.data.pretty_print.parts.rendering

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.styles.StyleCode



interface RenderParameters{
    val declaredWidth: Int
    val width: Int
    val trimTo: Int?
    val keySegmentSize: Int
    val projectedSize: Int
    val index: Int
    val orientation: Orientation
    val layout : RowLayout
}

interface CellRenderParameters: RenderParameters{
    fun renderComplete(record : RenderRecord)
    fun measureWidth(record: RenderRecord)
}



interface StyleParameters{
    val keyStyle: StyleCode
    val style: StyleCode
}



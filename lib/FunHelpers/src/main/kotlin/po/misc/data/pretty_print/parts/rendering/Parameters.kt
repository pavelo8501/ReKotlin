package po.misc.data.pretty_print.parts.rendering

import po.misc.data.pretty_print.parts.options.Borders
import po.misc.data.pretty_print.parts.options.Margins
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.RowLayout

class GridParameters(
    val rowOptions: RowOptions,
    val borders: Borders = Borders()
): RenderParameters{

    override val trimTo: Int? = null
    override val keySegmentSize: Int = 0
    override val declaredWidth: Int  get() = rowOptions.viewport.size
    override var width: Int = 0
    override val projectedSize: Int = 0
    override val index: Int = 0
    override val orientation: Orientation get() = rowOptions.orientation
    override val layout : RowLayout get() = rowOptions.layout
    val margins: Margins = Margins(0)
}

class RowParameters(
    val options: RowOptions,
): RenderParameters{

    override val trimTo: Int? = null
    override val keySegmentSize: Int = 0
    override val projectedSize: Int = 0
    override val orientation: Orientation get() = options.orientation
    override val layout : RowLayout get() = options.layout
    override var declaredWidth: Int = options.viewport.size


    override var width: Int = 0
        private set

    override val index: Int = 0
    internal val valuesText : String get() = buildString {
        append("RowLayout: $layout, ")
        append("Declared Width: $declaredWidth, ")
        append("Used width: $width, ")
    }
    override fun toString(): String = "RowParameters [$valuesText]"
}
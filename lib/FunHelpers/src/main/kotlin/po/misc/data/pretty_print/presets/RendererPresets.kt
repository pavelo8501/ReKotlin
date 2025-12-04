package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowRender

interface RendererPresets {

    val orientation: Orientation
    val outputIntegrity: Boolean

    fun toOptions(): RowRender{
       return  RowRender(this)
    }

    object OutputIntegrity : RendererPresets{
        override val orientation: Orientation = Orientation.Horizontal
        override val outputIntegrity: Boolean = true
    }
}
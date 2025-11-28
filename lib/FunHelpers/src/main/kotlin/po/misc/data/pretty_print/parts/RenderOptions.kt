package po.misc.data.pretty_print.parts

import po.misc.data.pretty_print.presets.RendererPresets
import po.misc.data.pretty_print.rows.PrettyRowBase

sealed interface CommonRenderOptions{
    val renderOnly: List<Enum<*>>
    val rowNoGap: Boolean
}

class RenderOptions(
    val orientation: Orientation?,
    val usePlain: Boolean = false,
    val renderLeftBorder: Boolean = true,
    val renderRightBorder: Boolean = true,
    override val rowNoGap: Boolean = true,
    override val renderOnly: List<Enum<*>> = emptyList()
):CommonRenderOptions {

    constructor(
        preset: RendererPresets,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
        rowNoGap: Boolean = true,
        renderOnly: List<Enum<*>> = emptyList()
    ):this(preset.orientation, usePlain, renderLeftBorder, renderRightBorder, rowNoGap,  renderOnly)

    constructor(
        vararg renderOnly: Enum<*>,
        orientation: Orientation? = null,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
        rowNoGap: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, rowNoGap,  renderOnly.toList())

    constructor(
        renderOnly: Collection<Enum<*>>,
        orientation: Orientation? = null,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
        rowNoGap: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, rowNoGap, renderOnly.toList())

    var canRecalculate: Boolean = true
        internal set

    var rowMaxSize: Int = 1
        internal set

    var cellsCount: Int = 1
        internal set

    fun setCellCount(count: Int):RenderOptions{
        cellsCount = count
        return this
    }

    fun assignParameters(prettyRow: PrettyRowBase):RenderOptions{
        if(canRecalculate){
            rowMaxSize = prettyRow.options.rowSize
            cellsCount = prettyRow.cells.size
        }
        return this
    }
    fun assignFinalize(prettyRow: PrettyRowBase):RenderOptions{
        rowMaxSize = prettyRow.options.rowSize
        cellsCount = prettyRow.cells.size
        canRecalculate = false
        return this
    }
    override fun toString(): String {
       return buildString {
            append("Use plain: $usePlain")
            append("; Left border: $renderLeftBorder")
            append("; Right border: $renderRightBorder")
        }
    }
}

class CellRender(
    val usePlain: Boolean = false,
    val renderLeftBorder: Boolean = true,
    val renderRightBorder: Boolean = true,
    override val renderOnly: List<Enum<*>> = emptyList()
):CommonRenderOptions {

    override val rowNoGap: Boolean = true

}


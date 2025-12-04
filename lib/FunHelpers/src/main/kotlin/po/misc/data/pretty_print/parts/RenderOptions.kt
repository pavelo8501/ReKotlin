package po.misc.data.pretty_print.parts

import po.misc.data.pretty_print.presets.RendererPresets
import po.misc.data.pretty_print.rows.PrettyRowBase

sealed class CommonRenderOptions(
    val usePlain: Boolean = false,
    val orientation: Orientation = Orientation.Horizontal,
    val renderLeftBorder: Boolean = true,
    val renderRightBorder: Boolean = true,
    val renderOnly: List<Enum<*>>,
){
    abstract var rowNoGap: Boolean

    var renderNamedOnly: Boolean = false

    var canRecalculate: Boolean = true
        internal set

    var rowMaxSize: Int = 1
        internal set

    var cellsCount: Int = 1
        internal set

    fun setCellCount(count: Int): CommonRenderOptions{
        cellsCount = count
        return this
    }
}

class RowRender(
    orientation: Orientation,
    usePlain: Boolean = false,
    renderLeftBorder: Boolean = true,
    renderRightBorder: Boolean = true,
    override var rowNoGap: Boolean = true,
    renderOnly: List<Enum<*>> = emptyList()
):CommonRenderOptions(usePlain, orientation, renderLeftBorder, renderRightBorder, renderOnly) {

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
        orientation: Orientation = Orientation.Horizontal,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
        rowNoGap: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, rowNoGap,  renderOnly.toList())

    constructor(
        renderOnly: Collection<Enum<*>>,
        orientation: Orientation = Orientation.Horizontal,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
        rowNoGap: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, rowNoGap, renderOnly.toList())

    constructor(
        rowOptions: RowOptions
    ):this(rowOptions.orientation, rowOptions.usePlain)

    override fun toString(): String {
       return buildString {
            append("Use plain: $usePlain")
            append("; Left border: $renderLeftBorder")
            append("; Right border: $renderRightBorder")
        }
    }
}

class CellRender(
    orientation: Orientation = Orientation.Horizontal,
    usePlain: Boolean = false,
    renderLeftBorder: Boolean = true,
    renderRightBorder: Boolean = true,
    renderOnly: List<Enum<*>> = emptyList(),
):CommonRenderOptions(usePlain, orientation,renderLeftBorder, renderRightBorder, renderOnly) {

    override var rowNoGap: Boolean = true

    constructor(
        vararg renderOnly: Enum<*>,
        orientation: Orientation = Orientation.Horizontal,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, renderOnly.toList())

    constructor(
        renderOnly: List<Enum<*>>,
        orientation: Orientation = Orientation.Horizontal,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true,
    ):this(orientation, usePlain, renderLeftBorder, renderRightBorder, renderOnly.toList())


    fun assignParameters(prettyRow: PrettyRowBase<*>):CellRender{
        if(canRecalculate){
            rowMaxSize = prettyRow.options.render.defaultWidth
            cellsCount = prettyRow.cells.size
        }
        return this
    }

    fun assignFinalize(prettyRow: PrettyRowBase<*>):CellRender{
        rowMaxSize = prettyRow.options.render.defaultWidth
        cellsCount = prettyRow.cells.size
        canRecalculate = false
        return this
    }

}


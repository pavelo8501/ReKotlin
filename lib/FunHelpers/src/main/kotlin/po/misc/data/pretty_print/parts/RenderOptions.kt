package po.misc.data.pretty_print.parts

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.presets.RendererPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.PrettyRowBase

class RenderOptions(
    val orientation: Orientation,
    val usePlain: Boolean = false,
    val renderLeftBorder: Boolean = true,
    val renderRightBorder: Boolean = true,
){

    constructor(
        preset: RendererPresets,
        usePlain: Boolean = false,
        renderLeftBorder: Boolean = true,
        renderRightBorder: Boolean = true
    ):this(preset.orientation, usePlain, renderLeftBorder, renderRightBorder){
        ensureOutputIntegrity = preset.outputIntegrity
    }

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

    var ensureOutputIntegrity: Boolean = false
        internal set


    fun isLastCell(index: Int): Boolean{
      return index == cellsCount - 1
    }

    fun isLastCell(cell: PrettyCellBase<*>): Boolean{
        return cell.index == cellsCount - 1
    }

    fun isFirstCell(index: Int): Boolean{
        return index == 0
    }
    fun isFirstCell(cell: PrettyCellBase<*>): Boolean{
        return cell.index == 0
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
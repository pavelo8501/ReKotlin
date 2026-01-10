package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.rendering.GridParameters
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.strings.appendGroup
import po.misc.data.styles.Colour

enum class Orientation{ Horizontal, Vertical }


class RowOptions(
    override var orientation : Orientation,
    var layout: RowLayout = RowLayout.Compact,
    override var viewport: ViewPortSize = Console220
): CommonRowOptions {

    constructor(orientation : Orientation,  viewport: ViewPortSize) : this(orientation,  RowLayout.Compact,  viewport)

    constructor(rowPreset: RowPresets) : this(rowPreset.orientation) {
        orientation =  rowPreset.orientation
        renderBorders = rowPreset.renderBorders
        cellOptions = PrettyHelper.toOptions(rowPreset.cellOptions)
    }

    var renderOnlyList: List<RowID> = listOf()
        internal set
    var excludeFromRenderList: List<RowID> = listOf()
        internal set
    var renderUnnamed: Boolean = true
        internal set
    override var plainKey: Boolean = false
        internal set

    var sealed: Boolean = false
        internal set
    var edited: Boolean = false
        internal set

    override var renderBorders: Boolean = true

    var cellSeparator: Border = Border(" | ", enabled = false)

    var borders: Borders = Borders()
        private set

    override var cellOptions: Options? = null

    fun borders(bottomBorder: Char, topBorder: Char? = null, sideBorders: Char = ' '){
        borders.bottomBorder =  Border(bottomBorder)
        topBorder?.let {
            borders.topBorder = Border(it)
        }
        borders.leftBorder = Border(sideBorders)
    }
    fun borders(borderColour: Colour, bottomBorder: Char, topBorder: Char? = null, sideBorders: Char? = null){
        borders.bottomBorder =  Border(bottomBorder, borderColour)
        topBorder?.let {
            borders.topBorder = Border(it, borderColour)
        }
        sideBorders?.let {
            borders.leftBorder = Border(it, borderColour)
        }
    }

    fun useId(rowId: RowID?):RowOptions{
        if(rowId != null){
            edited = true
        }
        return this
    }
    fun applyCellOptions(options : CellOptions?): RowOptions{
        if(options != null){
            cellOptions = PrettyHelper.toOptions(options)
        }
        return this
    }

    fun exclude(list: List<RowID>?, includeUnnamed: Boolean = true): RowOptions {
        excludeFromRenderList = list ?: emptyList()
        renderUnnamed = includeUnnamed
        renderOnlyList = emptyList()
        edited = true
        sealed = true
        return this
    }

    override fun asOptions(width: Int): Options = Options(this)
    override fun asRowOptions(): RowOptions = this

    private fun copyOptions():RowOptions{
        return RowOptions(orientation, layout, viewport).also {
            it.sealed = sealed
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.renderBorders = renderBorders
            it.borders = borders.copy()
            it.cellSeparator = cellSeparator.copy()
        }
    }
    fun copy(noEdit: Boolean = false): RowOptions {
        val opt = copyOptions()
        opt.sealed = noEdit
        return opt
    }
    fun copy(rowOrientation: Orientation, noEdit: Boolean = sealed): RowOptions {
        val opt = copyOptions()
        opt.orientation = rowOrientation
        opt.sealed = noEdit
        return opt
    }
    fun copy(rowId: RowID, rowOrientation: Orientation = orientation, noEdit: Boolean = sealed): RowOptions {
        val opt = copyOptions()
        opt.orientation = rowOrientation
        opt.sealed = noEdit
        return opt
    }
    fun copy(gridParameters: GridParameters): RowOptions {
        val copy = copyOptions()
        copy.viewport = gridParameters.rowOptions.viewport
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if(other !is RowOptions) return false
        if(other.layout != layout) return false
        if(other.orientation != orientation) return false
        if(other.viewport != viewport) return false
        return true
    }
    override fun hashCode(): Int {
        var result = (orientation.hashCode() ?: 0)
        result = 31 * result + layout.hashCode()
        result = 31 * result + plainKey.hashCode()
        result = 31 * result + orientation.hashCode()
        result = 31 * result + viewport.hashCode()
        return result
    }

    override fun toString(): String {
      return  buildString {
            appendLine("RowOptions[")
            append("Orientation: $orientation ")
            append("Edited: $edited ")
            append("MaxWidth: ${viewport.size} ")
            append("Layout: ${layout.name}]")
        }
    }
    companion object
}

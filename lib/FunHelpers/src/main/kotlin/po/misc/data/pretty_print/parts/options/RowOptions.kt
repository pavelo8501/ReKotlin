package po.misc.data.pretty_print.parts.options


import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.common.BorderInitializer
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.styles.SpecialChars


enum class Orientation(val separator: String){
    Horizontal(SpecialChars.EMPTY),
    Vertical(SpecialChars.NEW_LINE)
}

class RowOptions(
    override var orientation : Orientation,
    var layout: Layout = Layout.Compact,
): CommonRowOptions, BorderInitializer {

    constructor(
        orientation: Orientation,
        layout: Layout,
        viewportSize: ViewPortSize
    ): this(orientation, layout){
        viewport = viewportSize
    }

    constructor(
        orientation: Orientation,
        viewportSize: ViewPortSize,
        layout: Layout = Layout.Compact,
    ): this(orientation, layout){
        viewport = viewportSize
    }

    constructor(rowPreset: RowPresets) : this(rowPreset.orientation) {
        orientation =  rowPreset.orientation
        renderBorders = rowPreset.renderBorders
        cellOptions = PrettyHelper.toOptions(rowPreset.cellOptions)
    }

    override var viewport: ViewPortSize? = null
    var align: Align = Align.Left

    var renderOnlyList: List<RowID> = listOf()
        internal set
    var excludeFromRenderList: List<RowID> = listOf()
        internal set
    var renderUnnamed: Boolean = true
        internal set
    override var plainKey: Boolean = false
        internal set

    var edited: Boolean = false
        internal set

    override var renderBorders: Boolean = true

    var cellSeparator: InnerBorder = InnerBorder(" | ", enabled = false)

    val topBorder : TaggedSeparator<BorderPosition> = TaggedSeparator(BorderPosition.Top, "")
    val bottomBorder : TaggedSeparator<BorderPosition> = TaggedSeparator(BorderPosition.Bottom, "")
    val leftBorder : TaggedSeparator<BorderPosition> = TaggedSeparator(BorderPosition.Left, "")
    val rightBorder : TaggedSeparator<BorderPosition> = TaggedSeparator(BorderPosition.Right, "")

    override var separatorSet: List<TaggedSeparator<BorderPosition>> = listOf(topBorder, bottomBorder, leftBorder, rightBorder)

    override var cellOptions: Options? = null

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

//    fun exclude(list: List<RowID>?, includeUnnamed: Boolean = true): RowOptions {
//        excludeFromRenderList = list ?: emptyList()
//        renderUnnamed = includeUnnamed
//        renderOnlyList = emptyList()
//        edited = true
//        return this
//    }

    override fun asOptions(width: Int): Options = Options(this)
    override fun asRowOptions(): RowOptions = this

    private fun copyOptions():RowOptions{
        return RowOptions(orientation, layout).also {
            it.viewport = viewport
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.renderBorders = renderBorders
            it.separatorSet = listOf(topBorder.copy(), bottomBorder.copy(), leftBorder.copy(), rightBorder.copy())
            it.cellSeparator = cellSeparator.copy()
        }
    }
    fun copy(rowOrientation: Orientation): RowOptions {
        val opt = copyOptions()
        opt.orientation = rowOrientation
        return opt
    }
    fun copy(): RowOptions {
        val copy = copyOptions()
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
            append("ViewportSize: ${viewport?.size?:"N/A"} ")
            append("Layout: ${layout.name}]")
        }
    }
    companion object{
        operator fun invoke(
            orientation : Orientation = Orientation.Horizontal,
            builderAction: RowOptions.() -> Unit
        ):RowOptions{
            val row = RowOptions(orientation)
            builderAction.invoke(row)
            return row
        }
    }

}

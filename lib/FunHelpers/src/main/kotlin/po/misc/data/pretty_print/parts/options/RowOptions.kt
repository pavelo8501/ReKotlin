package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.strings.appendGroup

enum class Orientation{ Horizontal, Vertical }

sealed interface CommonRowOptions: PrettyOptions {
    val orientation : Orientation
    val renderBorders: Boolean
    val cellOptions: CommonCellOptions?
    fun asRowOptions():RowOptions
}

interface RowOptionsEditor{
    val edited: Boolean
    var orientation: Orientation
    fun useId(rowId: RowID?):RowOptions
    fun exclude(vararg excludeId: RowID, includeUnnamed: Boolean = true): RowOptions
    fun renderOnly(vararg renderOnlyId: RowID, includeUnnamed: Boolean = true): RowOptions
    fun noEdit(noEdit: Boolean = true):RowOptions
}

class RowOptions(
    override var orientation : Orientation,
    var layout: RowLayout = RowLayout.Compact
): CommonRowOptions, RowOptionsEditor {


    constructor(opts: CellOptions, orientation: Orientation = Orientation.Horizontal) : this(orientation) {
        cellOptions = PrettyHelper.toOptions(opts)
    }
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
    var plainText: Boolean = false
        internal set
    var usePlain: Boolean = false
        set(value) {
            plainKey = value
            plainText = value
            cellOptions?.plainKey = value
            cellOptions?.plainText = value
            field = value
        }
    var render: RenderDefaults = Console220
        internal set
    var sealed: Boolean = false
        internal set
    override var edited: Boolean = false
        internal set
    override var renderBorders: Boolean = true
    var borderSeparator: String = "|"

    override var cellOptions: Options? = null

    override fun noEdit(noEdit: Boolean):RowOptions{
        sealed = noEdit
        return this
    }
    override fun useId(rowId: RowID?):RowOptions{
        if(rowId != null){
            edited = true
        }
        return this
    }
    fun applyCellOptions(options : CellOptions?): RowOptions{
        if(options != null){
            cellOptions = PrettyHelper.Companion.toOptions(options)
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
    override fun exclude(vararg excludeId: RowID, includeUnnamed: Boolean): RowOptions =
        exclude(excludeId.toList(), includeUnnamed)
    fun renderOnly(list: List<RowID>?, includeUnnamed: Boolean = true): RowOptions {
        renderOnlyList = list ?: emptyList()
        renderUnnamed = includeUnnamed
        excludeFromRenderList = emptyList()
        edited = true
        sealed = true
        return this
    }
    override fun renderOnly(
        vararg renderOnlyId: RowID,
        includeUnnamed: Boolean
    ): RowOptions = renderOnly(renderOnlyId.toList(), includeUnnamed)
    override fun asOptions(width: Int): Options = Options(this)
    override fun asRowOptions(): RowOptions = this
    private fun copyKeyed():RowOptions{
        return RowOptions(orientation, layout).also {
            it.sealed = sealed
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
            it.renderBorders = renderBorders
        }
    }
    fun copy(noEdit: Boolean = false): RowOptions {
        val opt = copyKeyed()
        opt.sealed = noEdit
        return opt
    }
    fun copy(rowOrientation: Orientation, noEdit: Boolean = sealed): RowOptions {
        val opt = copyKeyed()
        opt.orientation = rowOrientation
        opt.sealed = noEdit
        return opt
    }
    fun copy(rowId: RowID, rowOrientation: Orientation = orientation, noEdit: Boolean = sealed): RowOptions {
        val opt = copyKeyed()
        opt.orientation = rowOrientation
        opt.sealed = noEdit
        return opt
    }

    override fun equals(other: Any?): Boolean {
        if(other !is RowOptions) return false
        if(other.layout != layout) return false
        if(other.orientation != orientation) return false
        if(other.usePlain != usePlain) return false
        if(other.render != render) return false
        return true
    }
    override fun hashCode(): Int {
        var result = (orientation.hashCode() ?: 0)
        result = 31 * result + layout.hashCode()
        result = 31 * result + plainKey.hashCode()
        result = 31 * result + plainText.hashCode()
        result = 31 * result + usePlain.hashCode()
        result = 31 * result + orientation.hashCode()
        result = 31 * result + render.hashCode()
        return result
    }
    override fun toString(): String {
      return  buildString {
            appendGroup("RowOptions[", "]", ::orientation, ::sealed, ::edited)
        }
    }
    companion object

}

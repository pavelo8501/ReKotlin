package po.misc.data.pretty_print.parts

import po.misc.data.strings.appendGroup


enum class Orientation{ Horizontal, Vertical }

sealed interface CommonRowOptions: PrettyOptions{
    val orientation : Orientation
    val renderBorders: Boolean
    val cellOptions: CommonCellOptions?
    fun asRowOptions():RowOptions
}

interface RowOptionsEditor{
    val edited: Boolean
    var orientation: Orientation
    fun useId(rowId: RowID?):RowOptions
    fun exclude(vararg excludeId:RowID, includeUnnamed: Boolean = true): RowOptions
    fun renderOnly(vararg renderOnlyId: RowID, includeUnnamed: Boolean = true): RowOptions
    fun noEdit(noEdit: Boolean = true):RowOptions
}


class RowOptions(): CommonRowOptions, RowOptionsEditor {

    constructor(orientation : Orientation):this(){
        this.orientation = orientation
    }
    constructor(rowId: RowID):this(){
        this.rowId = rowId
    }
    constructor(id: RowID, orientation: Orientation? = null):this(){
        this.rowId = id
        orientation?.let {
            this.orientation = it
        }
    }
    constructor(orientation: Orientation, id: RowID? = null):this(){
        this.orientation = orientation
        useId(id)
    }
    constructor(id: RowID, opts: CellOptions, orientation: Orientation? = null) : this() {
        rowId = id
        orientation?.let {
            this.orientation = it
        }
        cellOptions =  PrettyHelper.toOptions(opts)
    }

    constructor(rowPreset: RowPresets) : this() {
        orientation =  rowPreset.orientation
        renderBorders = rowPreset.renderBorders
        cellOptions = PrettyHelper.toOptions(rowPreset.cellOptions)
    }


    override var orientation: Orientation = Orientation.Horizontal
        set(value) {
            edited = true
            field = value
        }

    var rowId: RowID? = null
        internal set

    var renderOnlyList: List<RowID> = listOf()
        internal set
    var excludeFromRenderList: List<RowID> = listOf()
        internal set

    var renderUnnamed: Boolean = true
        internal set

    var plainKey: Boolean = false
        internal set

    var plainText: Boolean = false
        internal set

    var usePlain: Boolean = false
        set(value) {
            plainKey = value
            plainText = value
            field = value
        }

    var render: RenderDefaults = Console220
        internal set

    var sealed: Boolean = false
        internal set

    override var edited: Boolean = false
        internal set

    override var renderBorders: Boolean = true

    override var cellOptions: Options? = null

    override fun noEdit(noEdit: Boolean):RowOptions{
        sealed = noEdit
        return this
    }

    override fun useId(rowId: RowID?):RowOptions{
        if(rowId != null){
            this.rowId = rowId
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
        return RowOptions().also {
            it.sealed = sealed
            it.rowId = rowId
            it.orientation = orientation
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
    fun copy(rowId: RowID, rowOrientation: Orientation = orientation,  noEdit: Boolean = sealed): RowOptions {
        val opt = copyKeyed()
        opt.rowId = rowId
        opt.orientation = rowOrientation
        opt.sealed = noEdit
        return opt
    }

    fun applyChanges(other: RowOptions){
        rowId = other.rowId
        cellOptions = other.cellOptions
        renderUnnamed = other.renderUnnamed
        usePlain = other.usePlain
        render = other.render
        renderBorders = other.renderBorders
        if(other.cellOptions != null) {
            cellOptions = other.cellOptions
        }

    }

    override fun toString(): String {
      return  buildString {
            appendGroup("RowOptions[", "]", ::rowId, ::orientation, ::sealed, ::edited)
        }
    }

    companion object
}

package po.misc.data.pretty_print.parts


enum class Orientation{ Horizontal, Vertical }

sealed interface CommonRowOptions: PrettyOptions{
    val orientation : Orientation
    val cellOptions: CommonCellOptions?

    fun asRowOptions():RowOptions
}

interface RowOptionsEditor{
    val edited: Boolean
    var orientation: Orientation
    fun useId(id: Enum<*>):RowOptions
    fun exclude(vararg excludeId: Enum<*>, includeUnnamed: Boolean = true): RowOptions
    fun renderOnly(vararg renderOnlyId: Enum<*>, includeUnnamed: Boolean = true): RowOptions
}


class RowOptions(
    orientation : Orientation = Orientation.Horizontal,
    rowId: Enum<*>? = null
): CommonRowOptions, RowOptionsEditor {

    constructor(id: Enum<*>, orientation: Orientation? = null) : this(orientation?:Orientation.Horizontal, id)

    constructor(
        id: Enum<*>,
        opts: CellOptions,
        orientation: Orientation? = null
    ) : this(orientation?: Orientation.Horizontal) {
        rowId = id
        cellOptions = opts
    }

    override var orientation: Orientation = orientation
        set(value) {
            field = value
            edited = true
        }

    var rowId: Enum<*>? = rowId
        internal set

    var renderOnlyList: List<Enum<*>> = listOf()
        internal set
    var excludeFromRenderList: List<Enum<*>> = listOf()
        internal set

    var renderUnnamed: Boolean = true
        internal set

    var usePlain: Boolean = false
        internal set
    var render: RenderDefaults = Console220
        internal set

    var useNoEdit: Boolean = false
        internal set

    override var edited: Boolean = false
        internal set

    override var cellOptions: CellOptions? = null


    fun setNoEdit(noEdit: Boolean = true):RowOptions{
        useNoEdit = noEdit
        return this
    }

    override fun useId(id: Enum<*>):RowOptions{
        rowId = id
        edited = true
        return this
    }

    fun applyCellOptions(options : CellOptions?): RowOptions{
        if(options != null){
            cellOptions = options
        }
        return this
    }

    fun exclude(list: List<Enum<*>>?, includeUnnamed: Boolean = true): RowOptions {
        excludeFromRenderList = list ?: emptyList()
        renderUnnamed = includeUnnamed
        renderOnlyList = emptyList()
        edited =true
        return this
    }

    override fun exclude(vararg excludeId: Enum<*>, includeUnnamed: Boolean): RowOptions =
        exclude(excludeId.toList(), includeUnnamed)

    fun renderOnly(list: List<Enum<*>>?, includeUnnamed: Boolean = true): RowOptions {
        renderOnlyList = list ?: emptyList()
        renderUnnamed = includeUnnamed
        excludeFromRenderList = emptyList()
        edited = true
        return this
    }

    override fun renderOnly(vararg renderOnlyId: Enum<*>, includeUnnamed: Boolean): RowOptions =
        renderOnly(renderOnlyId.toList(), includeUnnamed)

    override fun asOptions(): Options = Options(this)

    override fun asRowOptions(): RowOptions = this

    fun copy(noEdit: Boolean = false): RowOptions {
        return RowOptions(orientation).also {
            it.useNoEdit = noEdit
            it.rowId = rowId
            it.orientation = orientation
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
        }
    }

    fun copy(newOrientation: Orientation, noEdit: Boolean? = null): RowOptions {
        return RowOptions(orientation).also {
            it.useNoEdit = noEdit?: useNoEdit
            it.rowId = rowId
            it.orientation = newOrientation
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
        }
    }

    fun copy(newRowId: Enum<*>, newOrientation: Orientation? = null,  noEdit: Boolean? = null): RowOptions {
        return RowOptions(orientation).also {
            it.useNoEdit =  noEdit?: useNoEdit
            it.rowId = newRowId
            it.orientation = newOrientation?:orientation
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
        }
    }
}

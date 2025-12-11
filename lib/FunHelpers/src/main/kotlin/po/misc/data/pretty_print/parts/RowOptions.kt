package po.misc.data.pretty_print.parts

import po.misc.data.strings.appendGroup


enum class Orientation{ Horizontal, Vertical }

sealed interface CommonRowOptions: PrettyOptions{
    val orientation : Orientation
    val cellOptions: CommonCellOptions?

    fun asRowOptions():RowOptions
}

interface RowOptionsEditor{
    val edited: Boolean
    var orientation: Orientation
    fun useId(rowId: Enum<*>?):RowOptions
    fun exclude(vararg excludeId: Enum<*>, includeUnnamed: Boolean = true): RowOptions
    fun renderOnly(vararg renderOnlyId: Enum<*>, includeUnnamed: Boolean = true): RowOptions
    fun noEdit(noEdit: Boolean = true):RowOptions
}


class RowOptions(

): CommonRowOptions, RowOptionsEditor {

    constructor(orientation : Orientation):this(){
        this.orientation = orientation
    }
    constructor(rowId: Enum<*>):this(){
        this.rowId = rowId
    }
    constructor(id: Enum<*>, orientation: Orientation? = null):this(){
        this.rowId = id
        orientation?.let {
            this.orientation = it
        }
    }
    constructor(orientation: Orientation, id: Enum<*>? = null):this(){
        this.orientation = orientation
        useId(id)
    }
    constructor(
        id: Enum<*>,
        opts: CellOptions,
        orientation: Orientation? = null
    ) : this() {
        rowId = id
        orientation?.let {
            this.orientation = it
        }
        cellOptions = opts
    }

    override var orientation: Orientation = Orientation.Horizontal
        set(value) {
            field = value
            edited = true
        }

    var rowId: Enum<*>? = null
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


    override fun noEdit(noEdit: Boolean):RowOptions{
        useNoEdit = noEdit
        return this
    }

    override fun useId(rowId: Enum<*>?):RowOptions{
        if(rowId != null){
            this.rowId = rowId
            edited = true
        }
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
        edited = true
        useNoEdit = true
        return this
    }

    override fun exclude(vararg excludeId: Enum<*>, includeUnnamed: Boolean): RowOptions =
        exclude(excludeId.toList(), includeUnnamed)

    fun renderOnly(list: List<Enum<*>>?, includeUnnamed: Boolean = true): RowOptions {
        renderOnlyList = list ?: emptyList()
        renderUnnamed = includeUnnamed
        excludeFromRenderList = emptyList()
        edited = true
        useNoEdit = true
        return this
    }

    override fun renderOnly(vararg renderOnlyId: Enum<*>, includeUnnamed: Boolean): RowOptions =
        renderOnly(renderOnlyId.toList(), includeUnnamed)

    override fun asOptions(): Options = Options(this)

    override fun asRowOptions(): RowOptions = this

    fun copy(noEdit: Boolean = false): RowOptions {
        return RowOptions().also {
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

    fun copy(orientation: Orientation, noEdit: Boolean? = null): RowOptions {
        return RowOptions(orientation).also {
            it.useNoEdit = noEdit?: useNoEdit
            it.rowId = rowId
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
        }
    }

    fun copy(rowId: Enum<*>, orientation: Orientation? = null,  noEdit: Boolean? = null): RowOptions {
        return RowOptions(rowId, orientation).also {
            it.useNoEdit =  noEdit?: useNoEdit
            it.renderOnlyList = renderOnlyList
            it.excludeFromRenderList = excludeFromRenderList
            it.renderUnnamed = renderUnnamed
            it.usePlain = usePlain
            it.render = render
        }
    }

    override fun toString(): String {
      return  buildString {
            appendGroup("RowOptions[", "]", ::rowId, ::orientation, ::useNoEdit, ::edited)
        }
    }
}

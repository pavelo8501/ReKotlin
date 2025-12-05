package po.misc.data.pretty_print.parts


enum class Orientation{ Horizontal, Vertical }

sealed interface CommonRowOptions: PrettyOptions{
    val  orientation : Orientation


}

class RowOptions(
    override var orientation : Orientation = Orientation.Horizontal,
): CommonRowOptions{

    constructor(
        renderDefault : RenderDefaults,
        orientation : Orientation = Orientation.Horizontal,
        rowId : Enum<*>? = null
    ):this(orientation){
        id = rowId
        render = renderDefault
    }

  //  constructor(id: Enum<*>, orientation : Orientation = Orientation.Horizontal):this(orientation, id)

    constructor(rowId: Enum<*>, vararg renderId:  Enum<*>, orientation : Orientation = Orientation.Horizontal):this(orientation = orientation){
        id = rowId
        renderOnlyList = renderId.toList()
    }
    var id : Enum<*>? = null
    var renderOnlyList: List<Enum<*>> = listOf()

    var usePlain: Boolean = false
    var render:  RenderDefaults  = Console220
        internal set

    fun renderOnly(list: List<Enum<*>>):RowOptions{
        renderOnlyList = list
        return this
    }

}

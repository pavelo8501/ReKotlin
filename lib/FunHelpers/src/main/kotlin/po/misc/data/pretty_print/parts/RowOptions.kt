package po.misc.data.pretty_print.parts



enum class Orientation{ Horizontal, Vertical }

class RowOptions(
    var orientation : Orientation = Orientation.Horizontal,
    var id : Enum<*>? = null
){

    constructor(
        renderDefault : RenderDefaults,
        orientation : Orientation = Orientation.Horizontal,
        id : Enum<*>? = null
    ):this(orientation, id){
        rowSize = renderDefault.defaultWidth
    }

    constructor(
        id : Enum<*>,
        orientation : Orientation = Orientation.Horizontal,
    ):this(orientation, id)



    var rowSize: Int  = Console220.defaultWidth
        internal set

}

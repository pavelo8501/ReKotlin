package po.misc.data.pretty_print.parts



enum class Orientation{ Horizontal, Vertical }

class RowOptions(
    var orientation : Orientation = Orientation.Horizontal,
    var id : Enum<*>? = null
): RowConfig{

    constructor(
        renderDefault : RenderDefaults,
        orientation : Orientation = Orientation.Horizontal,
        id : Enum<*>? = null
    ):this(orientation, id){
        render = renderDefault
    }

    constructor(
        id : Enum<*>,
        orientation : Orientation = Orientation.Horizontal,
    ):this(orientation, id)

    var usePlain: Boolean = false
    var render:  RenderDefaults  = Console220
        internal set

}

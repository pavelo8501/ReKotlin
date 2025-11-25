package po.misc.data.pretty_print.parts


import po.misc.data.pretty_print.rows.PrettyRow.Orientation


class RowOptions(
    var orientation : Orientation = Orientation.Horizontal,
){

    constructor(renderDefault : RenderDefaults, orientation : Orientation = Orientation.Horizontal):this(orientation){
        rowSize = renderDefault.defaultWidth
    }
    
    var rowSize: Int  = Console220.defaultWidth
        internal set

}

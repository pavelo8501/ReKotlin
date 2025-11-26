package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.parts.RowOptions


interface RowPresets{

    val orientation : Orientation

    fun toOptions(default:  RenderDefaults? = null): RowOptions{
       return if(default != null){
            RowOptions(default, orientation)
        }else{
            RowOptions(orientation)
        }
    }

    object VerticalRow : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
    }
    object HorizontalRow : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
    }

}
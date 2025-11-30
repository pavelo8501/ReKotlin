package po.misc.data.pretty_print.parts

interface RowPresets : RowConfig{
    val orientation : Orientation

    fun toOptions(default:  RenderDefaults? = null): RowOptions{
       return if(default != null){
            RowOptions(default, orientation)
        }else{
            RowOptions(orientation)
        }
    }

    object Vertical: RowPresets{
        override val orientation : Orientation = Orientation.Vertical
    }

    object Horizontal : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
    }

    object HeadedVertical : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
    }

    object HeadedHorizontal : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
    }

}
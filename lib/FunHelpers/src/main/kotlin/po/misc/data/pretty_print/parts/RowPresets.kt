package po.misc.data.pretty_print.parts


interface RowPresets : CommonRowOptions, RowConfig{

    override val orientation : Orientation

    fun toOptions(default:  RenderDefaults): RowOptions{
        return RowOptions(default, orientation)
    }

    fun toOptions():RowOptions{
       return RowOptions(orientation)
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
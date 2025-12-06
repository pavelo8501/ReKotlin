package po.misc.data.pretty_print.parts


interface RowPresets : CommonRowOptions{
    override val orientation : Orientation

    override fun asOptions(): Options = asOptions()
    override fun asRowOptions():RowOptions{
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
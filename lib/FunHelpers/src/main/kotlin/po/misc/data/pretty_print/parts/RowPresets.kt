package po.misc.data.pretty_print.parts


interface RowPresets : CommonRowOptions{

    override val orientation : Orientation
    override val cellOptions: CommonCellOptions?

    override fun asOptions(): Options = asOptions()
    override fun asRowOptions():RowOptions{
        return RowOptions(orientation).also {
            it.applyCellOptions(PrettyHelper.toOptionsOrNull(cellOptions))
        }
    }

    fun asRowOptions(buildAction: RowOptions.()-> Unit):RowOptions{
        val option =  RowOptions(orientation).also {
            it.applyCellOptions(PrettyHelper.toOptionsOrNull(cellOptions))
        }
        buildAction.invoke(option)
        return  option
    }

    fun asRowOptions(rowId: Enum<*>):RowOptions{
        return RowOptions(rowId, orientation).also {
            it.applyCellOptions(PrettyHelper.toOptionsOrNull(cellOptions))
        }
    }

    object Vertical: RowPresets{
        override val orientation : Orientation = Orientation.Vertical
        override val cellOptions: CellOptions? = null
    }

    object Horizontal : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
        override val cellOptions: CellOptions? = null
    }

    object HeadedVertical : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
        override val cellOptions: CellOptions? = null
    }
    object HeadedHorizontal : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
        override val cellOptions: CellPresets.Header =  CellPresets.Header
    }
}
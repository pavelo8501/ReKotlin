package po.misc.data.pretty_print.parts


interface RowPresets : CommonRowOptions{

    override val orientation : Orientation
    override val cellOptions: CommonCellOptions?

    override fun asOptions(width: Int): Options{
        val opt = PrettyHelper.toOptionsOrNull(cellOptions)
        val cellOptions =  Options(width)
        if(opt != null){
            cellOptions.applyChanges(opt)
        }
        return cellOptions
    }

    override fun asRowOptions():RowOptions{
        return RowOptions(orientation).also {
            val options = PrettyHelper.toOptionsOrNull(cellOptions)
            it.applyCellOptions(options)
        }
    }

    object Vertical : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
        override val renderBorders: Boolean = true
        override val cellOptions: CellOptions? = null
    }
    object Horizontal : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
        override val renderBorders: Boolean = true
        override val cellOptions: CellOptions? = null
    }

    object HorizontalBorderless : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
        override val renderBorders: Boolean = false
        override val cellOptions: CellOptions? = null
    }

    object VerticalHeaded : RowPresets{
        override val orientation : Orientation = Orientation.Vertical
        override val renderBorders: Boolean = true
        override val cellOptions: CellOptions? = null
    }
    object HorizontalHeaded : RowPresets{
        override val orientation : Orientation = Orientation.Horizontal
        override val renderBorders: Boolean = true
        override val cellOptions: CellPresets.Header =  CellPresets.Header
    }

    object VerticalPlain : RowPresets {
        override val orientation : Orientation = Orientation.Vertical
        override val renderBorders: Boolean = true
        override val cellOptions: CommonCellOptions = CellPresets.PlainText
    }

    object HorizontalPlain : RowPresets {
        override val orientation : Orientation = Orientation.Vertical
        override val renderBorders: Boolean = true
        override val cellOptions: CommonCellOptions = CellPresets.PlainText
    }

    object BulletList : RowPresets {
        override val orientation : Orientation = Orientation.Vertical
        override val renderBorders: Boolean = false
        override val cellOptions: CommonCellOptions get() {
            val opt = Options()
            opt.useForKey = "*"
            return opt
        }
    }

}
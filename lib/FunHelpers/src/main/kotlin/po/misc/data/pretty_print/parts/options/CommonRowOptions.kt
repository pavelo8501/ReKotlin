package po.misc.data.pretty_print.parts.options


sealed interface CommonRowOptions: PrettyOptions, RowBuildOption {
    val orientation : Orientation
    val renderBorders: Boolean
    val cellOptions: CellOptions?
    val viewport: ViewPortSize?
    fun asRowOptions():RowOptions
}

package po.misc.data.pretty_print.grid

import po.misc.collections.asList
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.RowContainer


inline fun <T: Any, reified V: Any> GridContainerBase<T, V>.addHeadedRow(
    text: String,
    id: Enum<*>? = null,
    rowPreset: RowPresets = RowPresets.HeadedHorizontal,
): PrettyRow<V> {
    val options = rowPreset.asRowOptions{
        if(id != null){
            useId(id)
        }
    }
    val cell =  StaticCell(text).applyOptions(options.cellOptions)
    val row =  PrettyRow<V>(type, options, cell.asList())
    when(this){
        is GridContainer<*> -> { addRow(row) }
        else -> { addRow(row) }
    }
    return row
}


inline fun <T: Any, reified V: Any> GridContainerBase<T, V>.addHeadedRow(
    text: String,
    rowPreset: RowPresets = RowPresets.HeadedHorizontal,
): PrettyRow<V> = addHeadedRow(text, null, rowPreset)

fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    rowId: Enum<*>,
    orientation: Orientation? = null,
    vararg cells: PrettyCellBase
):  PrettyRow<V>{

    val opts =  RowOptions(rowId, orientation)
    val row =  PrettyRow<V>(type, opts, cells.toList())
    when(this){
        is  GridContainer<*> -> {
            addRow(row)
        }
        else -> {
            addRow(row)
        }
    }
    return row
}

fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    rowId: Enum<*>,
    vararg cells: PrettyCellBase
):  PrettyRow<V>  = createRow(rowId, *cells)


fun <T: Any> GridContainer<T>.buildRow(
    orientation: Orientation,
    builder: RowContainer<T>.() -> Unit
): Unit = buildRow(RowOptions(orientation), builder)

fun <T: Any> GridContainer<T>.buildRow(
    rowId: Enum<*>,
    orientation: Orientation = Orientation.Horizontal,
    builder: RowContainer<T>.() -> Unit
): Unit {
    val opts = RowOptions(orientation, rowId).noEdit()
    buildRow(opts, builder)
}



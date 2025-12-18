package po.misc.data.pretty_print.grid

import po.misc.collections.asList
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.rows.RowBuilderScope
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowContainerBase


fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    rowId: RowID,
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
    builder: RowContainerBase<T, T>.() -> Unit
): Unit = buildRow(RowOptions(orientation), builder)


fun <T: Any> GridContainer<T>.buildRow(
    rowId: RowID,
    orientation: Orientation = Orientation.Horizontal,
    builder: RowContainerBase<T, T>.() -> Unit
): Unit {
    val opts = RowOptions(orientation, rowId).noEdit()
    buildRow(opts, builder)
}



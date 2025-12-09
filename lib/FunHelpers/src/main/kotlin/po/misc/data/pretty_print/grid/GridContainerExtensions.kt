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
    rowPreset: RowPresets = RowPresets.HeadedHorizontal,
): PrettyRow<V> {
    val options = rowPreset.asRowOptions()
    val cell =  StaticCell(text).applyOptions(options.cellOptions)
    return createRow(options, cell.asList())
}

inline fun <T: Any, reified V: Any> GridContainerBase<T, V>.addHeadedRow(
    text: String,
    id: Enum<*>,
    rowPreset: RowPresets = RowPresets.HeadedHorizontal,
): PrettyRow<V> {
    val options =   rowPreset.asRowOptions(){
        useId(id)
    }
    val cell = StaticCell(text).applyOptions(options.cellOptions)
    return createRow(options, cell.asList())
}

fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    orientation: Orientation,
    vararg cells: PrettyCellBase
):  PrettyRow<V> = createRow(RowOptions(orientation), cells.toList())

fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    rowId: Enum<*>,
    vararg cells: PrettyCellBase
):  PrettyRow<V> = createRow(RowOptions(rowId), cells.toList())

fun <T: Any, V: Any> GridContainerBase<T, V>.createRow(
    rowId: Enum<*>,
    orientation: Orientation = Orientation.Horizontal
):  PrettyRow<V> = createRow(RowOptions(orientation, rowId))

fun <T: Any> GridContainer<T>.buildRow(
    orientation: Orientation,
    builder: RowContainer<T>.() -> Unit
): Unit = buildRow(RowOptions(orientation), builder)


fun <T: Any> GridContainer<T>.buildRow(
    rowId: Enum<*>,
    orientation: Orientation = Orientation.Horizontal,
    builder: RowContainer<T>.() -> Unit
): Unit {
    val opts = RowOptions(orientation, rowId).setNoEdit()
    buildRow(opts, builder)
}



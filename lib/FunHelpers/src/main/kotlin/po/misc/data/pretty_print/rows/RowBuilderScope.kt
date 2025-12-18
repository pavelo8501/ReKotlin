package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.types.token.TypeToken

interface RowBuilderScope<V: Any> {

    val type: TypeToken<V>

    fun addRow(row: PrettyRow<V>): GridKey?

    fun  RowBuilderScope<V>.headedRow(
        text: String,
        id: RowID? = null,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<V> {
        val options = rowPreset.asRowOptions().useId(id)
        val cell =  StaticCell(text).applyOptions(options.cellOptions)
        val row =  PrettyRow<V>(type, options, cell.asList())
        addRow(row)
        return row
    }

    fun RowBuilderScope<V>.headedRow(
        text: String,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<V> = headedRow(text, null, rowPreset)


    @PrettyDSL
    fun buildRow(
        rowId:RowID,
        orientation: Orientation = Orientation.Horizontal,
        builder: RowContainer<V>.() -> Unit
    ){
        val container = createRowContainer(type, RowOptions(rowId, orientation))
        val row =  container.applyBuilder(builder)
        addRow(row)
    }

    @PrettyDSL
    fun buildRow(
        orientation: Orientation,
        builder: RowContainer<V>.() -> Unit
    ){
        val container = createRowContainer(type, RowOptions(orientation))
        val row =  container.applyBuilder(builder)
        addRow(row)
    }
}


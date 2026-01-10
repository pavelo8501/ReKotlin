package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.types.token.TypeToken


interface RowBuilderScope<T>{

    val type: TypeToken<T>
    val options: RowOptions?
    fun addRow(row: PrettyRow<T>): PrettyRow<T>
    fun exclude(vararg id: RowID) = options?.exclude(id.toList())

   fun  headedRow(
        text: String,
        rowID: RowID? = null,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<T> {
        val options = rowPreset.asRowOptions().useId(rowID)
        val cell = StaticCell(text).applyOptions(options.cellOptions)
        val row =  PrettyRow(type,rowID, options, cell.asList())
        addRow(row)
        return row
    }

    fun RowBuilderScope<T>.headedRow(
        text: String,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<T> = headedRow(text, null, rowPreset)

}


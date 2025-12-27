package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.BuilderScope
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.types.token.TypeToken


interface RowBuilderScope<T>{

    val type: TypeToken<T>
    val options: RowOptions?
    fun addRow(row: PrettyRow<T>): PrettyRow<T>
    fun exclude(vararg id: RowID) = options?.exclude(id.toList())
    fun useId(id: GridID){

    }
    
   fun  RowBuilderScope<T>.headedRow(
        text: String,
        id: RowID? = null,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<T> {
        val options = rowPreset.asRowOptions().useId(id)
        val cell = StaticCell(text).applyOptions(options.cellOptions)
        val row =  PrettyRow<T>(cell.asList(), type, options)
        addRow(row)
        return row
    }

    fun RowBuilderScope<T>.headedRow(
        text: String,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<T> = headedRow(text, null, rowPreset)


    @PrettyDSL
    fun buildRow(
        rowId:RowID? = null,
        builder: RowBuilder<T>.() -> Unit
    ){
        val container = createRowContainer(type, rowId)
        builder.invoke(container)
        val row =  container.finalizeRow()
        addRow(row)
    }

}


package po.misc.data.pretty_print

import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.grid.HostGridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken


interface Templated<T> : PrettyBuilder, TokenFactory{

    val type: TypeToken<T>

    fun buildOption(
        builder:  Options.()-> Unit
    ) : Options = dslEngine.buildOption(builder)


    fun buildOption(
        preset: CellPresets,
        builder:  Options.()-> Unit
    ) : Options = dslEngine.buildOption(preset, builder)


    fun buildRowOption(
        builder:  RowOptions.()-> Unit
    ) : RowOptions = dslEngine.buildRowOption(builder)

    fun buildRowOption(
        preset: RowPresets,
        builder:  RowOptions.()-> Unit
    ) : RowOptions = dslEngine.buildRowOption(preset,  builder)


    @PrettyDSL
    fun buildRow(
        rowId: RowID? = null,
        builder: RowBuilder<T>.()-> Unit
    ): PrettyRow<T> {
       val container = dslEngine.prepareRow(token= type, rowId,  builder)
       return container.finalizeRow()
    }

    @PrettyDSL
    fun buildGrid(
        gridID: GridID? = null,
        builder: HostGridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
      return  dslEngine.prepareGrid(type, gridID, builder).finalizeGrid(null)
    }
    companion object{
        internal val dslEngine = DSLEngine()
    }
}



inline fun <reified T: Any> Templated<T>.buildRow(
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> {
    val container = RowBuilder<T>(TypeToken.create<T>())
    builder.invoke(container)
    return container.finalizeRow()
}


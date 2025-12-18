package po.misc.data.pretty_print

import po.misc.data.pretty_print.grid.GridContainer
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken


interface Templated<T : Any> : PrettyBuilder, TokenFactory{

    val valueType: TypeToken<T>

    fun buildOption(cellOption: Options.Companion,  builder:  Options.()-> Unit) : Options{
        val opt = Options()
        opt.builder()
        return opt
    }
    fun buildOption(preset: CellPresets, builder:  Options.()-> Unit) : Options{
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildOption(rowOptions: RowOptions.Companion, builder:  RowOptions.()-> Unit) : RowOptions{
        val opt = RowOptions()
        opt.builder()
        return opt
    }

    fun buildGrid(
        rowId: RowID? = null,
        builder: GridContainer<T>.() -> Unit
    ): PrettyGrid<T> {
        val container = GridContainer(valueType, RowOptions().useId(rowId) )
        builder.invoke(container)
        return container.initGrid()
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<T>.()-> Unit
    ): PrettyRow<T> {
        val container = RowContainer<T>(valueType, PrettyHelper.toRowOptions(rowOptions))
        builder.invoke(container)
        return container.initRow()
    }
}

inline fun <reified T: Any> Templated<T>.buildRow(
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowContainer<T>.()-> Unit
): PrettyRow<T> {
    val container = RowContainer<T>(TypeToken.create<T>(), PrettyHelper.toRowOptions(rowOptions))
    builder.invoke(container)
    return container.initRow()
}


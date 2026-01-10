package po.misc.data.pretty_print

import po.misc.callbacks.callable.ProviderProperty
import po.misc.callbacks.callable.ProviderProperty.Companion.invoke
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


interface Templated<T> : TokenFactory{

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
    ) : RowOptions = dslEngine.buildRowOption(builder = builder)

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
        builder: GridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
       val container =  dslEngine.prepareGrid(type, gridID, builder)
       return container.prettyGrid
    }

    fun String.toCell(opts: CellOptions? = null): StaticCell{
        return StaticCell(this, opts)
    }

    fun KProperty1<T, *>.toCell(opts: CellOptions? = null): KeyedCell<T>{
        return KeyedCell(type, this,  opts)
    }

    fun KProperty0<*>.toCell(opts: CellOptions? = null): KeyedCell<T>{
        ProviderProperty(type, this)
        return KeyedCell<T>(ProviderProperty(type, this), opts)
    }

    fun KProperty1<T, String>.toCell(
        opts: CellOptions? = null,
        builder: ComputedCell<T, String>.(String)-> Any
    ): ComputedCell<T, String>{
      return  ComputedCell(type, this, opts, builder = builder)
    }

    companion object{
        internal val dslEngine = DSLEngine()
    }
}


@PrettyDSL
inline fun <reified T2>  Templated<*>.buildGrid(
    gridID: GridID? = null,
    builderAction: GridBuilder<T2>.() -> Unit
): PrettyGrid<T2> {
    val builder =  GridBuilder<T2>(gridID)
    builderAction.invoke(builder)
    builder.prettyGrid
    return  builder.finalizeGrid()
}

inline fun <reified T: Any> Templated<T>.buildRow(
    rowID: RowID? = null,
    noinline builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> {
    val container = RowBuilder<T>(rowID)
    builder.invoke(container)
    return container.finalizeRow()
}

inline fun <S, reified T> Templated<S>.toCell(
    prop:  KProperty1<S, T>,
    noinline builder: ComputedCell<S, T>.(T)-> Any
): ComputedCell<S, T>{
    return ComputedCell(type, prop, builder = builder)
}

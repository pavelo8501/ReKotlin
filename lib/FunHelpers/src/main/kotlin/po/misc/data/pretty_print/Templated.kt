package po.misc.data.pretty_print

import po.misc.callbacks.callable.ProviderProperty
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.parts.common.PrettyPrintAuxBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.*
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


interface Templated<T>: PrettyPrintAuxBuilder, TokenFactory{

    val receiverType: TypeToken<T>

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
       val container = dslEngine.prepareRow(token= receiverType, rowId,  builder)
       return container.finalizeRow()
    }

    @PrettyDSL
    fun buildGrid(
        gridID: GridID? = null,
        builderAction: GridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
        val container =  GridBuilder(receiverType, gridID)
        builderAction.invoke(container)
        return  container.finalizeGrid()
    }

    fun String.toCell(opts: CellOptions? = null): StaticCell =
         StaticCell(this, opts)

    fun KProperty1<T, *>.toCell(opts: CellOptions? = null): KeyedCell<T> =
         KeyedCell(receiverType, this,  opts)

    fun KProperty0<*>.toCell(opts: CellOptions? = null): KeyedCell<T> =
         KeyedCell(ProviderProperty(receiverType, this), opts)

    fun KProperty1<T, String>.toCell(
        opts: CellOptions? = null,
        builder: ComputedCell<T, String>.(String)-> Any
    ): ComputedCell<T, String> = ComputedCell(receiverType, this, opts, builder = builder)

    companion object{
        internal val dslEngine = DSLEngine()
    }
}

inline fun <reified T> Templated<*>.toCell(prop : KProperty1<T, *>,  opts: CellOptions? = null): KeyedCell<T>{
    return KeyedCell(TypeToken<T>(), prop,  opts)
}

@PrettyDSL
inline fun <reified T2> Templated<*>.buildGrid(
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
    return ComputedCell(receiverType, prop, builder = builder)
}

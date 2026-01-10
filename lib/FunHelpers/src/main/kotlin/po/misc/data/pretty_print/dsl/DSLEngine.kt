package po.misc.data.pretty_print.dsl

import po.misc.callbacks.callable.asPropertyCallable
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1

@PublishedApi
internal open class DSLEngine: TokenFactory, PrettyHelper{

    fun buildOption(

        builder:  Options.()-> Unit
    ) : Options{
        val opt = Options()
        opt.builder()
        return opt
    }
    fun buildOption(preset: CellPresets, builder:  Options.()-> Unit) : Options{
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        orientation: Orientation = Orientation.Horizontal,
        layout: RowLayout = RowLayout.Compact,
        render: ViewPortSize = Console220,
        builder:  RowOptions.()-> Unit
    ) : RowOptions{
        val opt = RowOptions(orientation, layout, render)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        rowPreset: RowPresets,
        builder:  RowOptions.()-> Unit
    ) : RowOptions {

        val opt = RowOptions(rowPreset)
        opt.builder()
        return opt
    }

    @PrettyDSL
    inline fun <reified T> createGrid(
        gridID: GridID? = null,
    ): PrettyGrid<T>{
       return PrettyGrid(tokenOf<T>(), gridID =  gridID)
    }

    @PrettyDSL
    inline fun <reified T> buildRow(
        rowID: RowID? = null,
        builder: RowBuilder<T>.()-> Unit
    ): PrettyRow<T> {
        val container = RowBuilder<T>(rowID)
        builder.invoke(container)
        return container.finalizeRow()
    }

    @PrettyDSL
    fun <T> prepareRow(
        token: TypeToken<T>,
        rowID: RowID? = null,
        builder: RowBuilder<T>.()-> Unit
    ): RowBuilder<T> {
        val container = RowBuilder(token, rowID)
        builder.invoke(container)
        return container
    }

    @PrettyDSL
    inline fun <T, reified V> prepareRow(
        token: TypeToken<T>,
        property: KProperty1<T, V>,
        rowID: RowID? = null,
        noinline builderAction: ValueRowBuilder<T, V>.()-> Unit
    ): ValueRowBuilder<T, V> {
        val callable = property.asPropertyCallable(token)
        val container = ValueRowBuilder(token,callable.receiverType, rowID)
        container.dataLoader.add(callable)
        container.preSaveBuilder(builderAction)
        return container
    }

    @PrettyDSL
    inline fun <T, reified V> prepareListRow(
        token: TypeToken<T>,
        property: KProperty1<T, List<V>>,
        rowID: RowID? = null,
        noinline builderAction: ValueRowBuilder<T, V>.()-> Unit
    ): ValueRowBuilder<T, V> {
        val callable = property.asPropertyCallable(token)
        val container = ValueRowBuilder(token, TypeToken<V>(), rowID)
        container.dataLoader.add(callable)
        container.preSaveBuilder(builderAction)
        return container
    }

    @PrettyDSL
    fun <T> prepareGrid(
        token: TypeToken<T>,
        gridID: GridID? = null,
        builder: GridBuilder<T>.() -> Unit
    ): GridBuilder<T> {
        val container =  GridBuilder(token, gridID)
        builder.invoke(container)
        return container
    }
}
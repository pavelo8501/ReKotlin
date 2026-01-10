package po.misc.data.pretty_print.dsl

import org.jetbrains.annotations.TestOnly
import po.misc.callbacks.callable.asPropertyCallable
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


@TestOnly
class InTestDSL:  TokenFactory, PrettyHelper {

    internal val prettyDSL = DSLEngine()


    fun buildOption(builder: Options.() -> Unit): Options {
        val opt = Options()
        opt.builder()
        return opt
    }

    fun buildOption(preset: CellPresets, builder: Options.() -> Unit): Options {
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildRowOption(preset: CellPresets, builder: Options.() -> Unit): Options {
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        builder: RowOptions.() -> Unit
    ): RowOptions {
        val opt = RowOptions(Orientation.Horizontal)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        presets: RowPresets,
        builder: RowOptions.() -> Unit
    ): RowOptions {
        val opt = RowOptions(presets)
        opt.builder()
        return opt
    }

    @PrettyDSL
    inline fun <reified T> createGrid(
        gridID: GridID? = null,
    ): PrettyGrid<T> {
        return PrettyGrid(tokenOf<T>(), gridID = gridID)
    }

    @PrettyDSL
    inline fun <reified T, reified V> createValueGrid(
        prop: KProperty1<T, V>,
        gridID: GridID? = null,
    ): PrettyValueGrid<T, V> {
        val callable = prop.asPropertyCallable()
        val valueGrid = PrettyValueGrid(tokenOf<T>(), callable.receiverType, gridID = gridID)
        valueGrid.dataLoader.add(callable)
        return valueGrid
    }

//    @PrettyDSL
//    inline fun <T, reified V> buildGrid(
//        token: TypeToken<T>,
//        property: KProperty1<T, V>,
//        gridID: GridID? = null,
//        builder: ValueGridBuilder<T, V>.() -> Unit
//    ): ValueGridBuilder<T, V> {
//        val provider = property.toElementProvider(token)
//
//        val container = ValueGridBuilder(provider, gridID)
//        builder.invoke(container)
//        return container
//    }

//
//    @PrettyDSL
//    inline fun <T, reified V> buildGrid(
//        hostGrid: PrettyGridBase<T>,
//        property: KProperty1<T, V>,
//        gridID: GridID? = null,
//        builder: ValueGridBuilder<T, V>.() -> Unit
//    ): ValueGridBuilder<T, V> {
//
//        val provider = property.toElementProvider(hostGrid.rec)
//
//        val provider = property.toElementProvider(token)
//        val container = ValueGridBuilder(provider, gridID)
//        builder.invoke(container)
//        return container
//    }
//

    @PrettyDSL
    inline fun <reified T> buildRow(
        rowID: RowID? = null,
        builder: RowBuilder<T>.() -> Unit
    ): PrettyRow<T> {
        val container = RowBuilder(tokenOf<T>(), rowID)
        builder.invoke(container)
        return container.finalizeRow()
    }

    @PrettyDSL
    fun <T> prepareRow(
        token: TypeToken<T>,
        rowID: RowID? = null,
        builder: RowBuilder<T>.() -> Unit
    ): RowBuilder<T> =  prettyDSL.prepareRow(token, rowID, builder)

//    @PrettyDSL
//    inline fun <T, reified V> prepareRow(
//        token: TypeToken<T>,
//        property: KProperty1<T, V>,
//        rowID: RowID? = null,
//        builder: ValueRowBuilder<T, V>.() -> Unit
//    ): ValueRowBuilder<T, V> {
//        val provider = property.toElementProvider(token)
//
//        val container = ValueRowBuilder(provider, rowID)
//        builder.invoke(container)
//        return container
//    }

//    @PrettyDSL
//    inline fun <T, reified V> prepareListRow(
//        token: TypeToken<T>,
//        property: KProperty1<T, List<V>>,
//        rowID: RowID? = null,
//        builder: ValueRowBuilder<T, V>.() -> Unit
//    ): ValueRowBuilder<T, V> {
//        val provider = property.toListProvider(token,)
//        val container = ValueRowBuilder(provider, rowID)
//        builder.invoke(container)
//        return container
//    }


    fun <T> buildGridGridReturnBuilder(
        token: TypeToken<T>,
        gridID: GridID? = null,
        builder: GridBuilder<T>.() -> Unit
    ): GridBuilder<T> {
        val container = GridBuilder(token, gridID)
        builder.invoke(container)
        return container
    }

    inline fun <reified T> buildGridGridReturnBuilder(
        gridID: GridID? = null,
        noinline builder: GridBuilder<T>.() -> Unit
    ): GridBuilder<T> = buildGridGridReturnBuilder(tokenOf<T>(), gridID, builder)

    @PrettyDSL
    inline fun <reified T> buildGrid(
        gridID: GridID? = null,
        builder: GridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
        val container = GridBuilder(tokenOf<T>(), gridID)
        builder.invoke(container)
        return container.finalizeGrid()
    }
}
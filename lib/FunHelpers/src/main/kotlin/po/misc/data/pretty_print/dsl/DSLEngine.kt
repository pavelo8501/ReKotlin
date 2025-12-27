package po.misc.data.pretty_print.dsl

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.grid.HostGridBuilder
import po.misc.data.pretty_print.grid.ValueGridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.data.pretty_print.toProvider
import po.misc.types.castOrThrow
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1




@PublishedApi
internal open class DSLEngine: TokenFactory, PrettyHelper{

    fun buildOption(builder:  Options.()-> Unit) : Options{
        val opt = Options()
        opt.builder()
        return opt
    }
    fun buildOption(preset: CellPresets, builder:  Options.()-> Unit) : Options{
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildRowOption(preset: CellPresets, builder:  Options.()-> Unit) : Options{
        val opt = Options(preset)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        builder:  RowOptions.()-> Unit
    ) : RowOptions{
        val opt = RowOptions(Orientation.Horizontal)
        opt.builder()
        return opt
    }

    fun buildRowOption(
        presets: RowPresets,
        builder:  RowOptions.()-> Unit
    ) : RowOptions{
        val opt = RowOptions(presets)
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
    inline fun <T, reified V> buildGrid(
        token:TypeToken<T>,
        property: KProperty1<T, V>,
        gridID: GridID? = null,
        builder: ValueGridBuilder<T, V>.() -> Unit
    ):ValueGridBuilder<T, V>{
        val provider =  property.toProvider(token)
        val container = ValueGridBuilder(provider, gridID)
        builder.invoke(container)
        return container
    }

    @PrettyDSL
    inline fun <reified T> buildRow(
        rowID: RowID? = null,
        builder: RowBuilder<T>.()-> Unit
    ): PrettyRow<T> {
        val container = RowBuilder(tokenOf<T>(), rowID)
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
        builder: ValueRowBuilder<T, V>.()-> Unit
    ): ValueRowBuilder<T, V> {
        val provider = property.toProvider(token)
        val container = ValueRowBuilder(provider, rowID)
        builder.invoke(container)
        return container
    }

    @PrettyDSL
    inline fun <T, reified V> prepareListRow(
        token: TypeToken<T>,
        property: KProperty1<T, List<V>>,
        rowID: RowID? = null,
        builder: ValueRowBuilder<T, V>.()-> Unit
    ): ValueRowBuilder<T, V> {
        val provider = property.toProvider(token, tokenOf<V>())
        val container = ValueRowBuilder(provider, rowID)
        builder.invoke(container)
        return container
    }

    @PrettyDSL
    fun <T> prepareGrid(
        token: TypeToken<T>,
        gridID: GridID? = null,
        builder: HostGridBuilder<T>.() -> Unit
    ): HostGridBuilder<T> {
        val container =  HostGridBuilder(token, gridID)
        builder.invoke(container)
        return container
    }

    @PrettyDSL
    inline fun <reified T> prepareGrid(
        gridID: GridID? = null,
        noinline  builder: HostGridBuilder<T>.() -> Unit
    ): HostGridBuilder<T> = prepareGrid(tokenOf<T>(), gridID, builder)

    @PrettyDSL
    inline fun <reified T> buildGrid(
        gridID: GridID? = null,
        builder: HostGridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
        val container =  HostGridBuilder(tokenOf<T>(), gridID)
        builder.invoke(container)
        return container.finalizeGrid(null).castOrThrow()
    }
}
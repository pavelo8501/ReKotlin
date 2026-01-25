package po.misc.data.pretty_print.dsl

import org.jetbrains.annotations.TestOnly
import po.misc.callbacks.callable.toCallable
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.types.token.TokenFactory
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


@TestOnly
class InTestDSL:  TokenFactory, PrettyHelper {

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
        return PrettyValueGrid( prop.toCallable(), gridID = gridID)
    }

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
    inline fun <reified T> buildGrid(
        gridID: GridID? = null,
        builder: GridBuilder<T>.() -> Unit
    ): PrettyGrid<T> {
        val container = GridBuilder(tokenOf<T>(), gridID)
        builder.invoke(container)
        return container.finalizeGrid()
    }
}
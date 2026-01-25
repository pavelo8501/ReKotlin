package po.misc.data.pretty_print.grid

import po.misc.callbacks.callable.toCallable
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


class GridBuilder<T>(
    receiverType:TypeToken<T>,
    gridID: GridID? = null,
    opts:CommonRowOptions? = null,
): GridBuilderBase<T>(receiverType){

    override val prettyGrid:PrettyGrid<T> = PrettyGrid(receiverType, gridID, opts)

    override fun addRow(row: PrettyRow<T>): PrettyRow<T> {
        prettyGrid.addRow(row)
        return row
    }

    fun finalizeGrid(): PrettyGrid<T> = super.finalizeGrid(prettyGrid) as PrettyGrid<T>

    @PrettyDSL
    inline fun <reified V> buildGrid(
        property: KProperty1<T, V>,
        gridID: GridID? = null,
        builderAction: ValueGridBuilder<T, V>.() -> Unit
    ): PrettyValueGrid<T, V>{
        val callable = property.toCallable(receiverType)
        val valueGridBuilder = ValueGridBuilder(callable, gridID)
        builderAction.invoke(valueGridBuilder)
        return  valueGridBuilder.finalizeGrid(prettyGrid)
    }

    @PrettyDSL
    inline fun <reified V> buildListGrid(
        listProperty:KProperty1<T, List<V>>,
        gridID: GridID? = null,
        builderAction: ValueGridBuilder<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        val callable = listProperty.toCallable(receiverType)
        val valueGridBuilder = ValueGridBuilder(property =  callable,  gridID)
        builderAction.invoke(valueGridBuilder)
        return valueGridBuilder.finalizeGrid(prettyGrid)
    }


    @PrettyDSL
    fun <V: Any> useTemplate(
        container: ValueGridBuilder<T, V>,
        gridID: GridID? = null,
        opts: CommonRowOptions? = null,
        builderAction: (ValueGridBuilder<T, V>.() -> Unit)? = null
    ):PrettyValueGrid<T, V>{
        opts?.let {
            container.applyOptions(it)
        }
        builderAction?.invoke(container)
        return container.finalizeGrid(prettyGrid)
    }

    @PrettyDSL
    inline fun <reified V> useGrid(
        grid: PrettyGrid<V>,
        property: KProperty1<T, V>,
        gridID: GridID? = null,
        noinline builderAction: (ValueGridBuilder<T, V>.() -> Unit)? = null
    ): PrettyValueGrid<T, V> {
        val gridCopy = grid.copy()
        val callable = property.toElementProvider(receiverType, tokenOf<V>())
        val valueGridBuilder = ValueGridBuilder(callable, gridID)
        valueGridBuilder.addRowsChecking(callable.receiverType,  gridCopy.rows)
        builderAction?.invoke(valueGridBuilder)
        return valueGridBuilder.finalizeGrid(prettyGrid)
    }

    @PrettyDSL
    @JvmName("useGridList")
    inline fun <reified V> useGrid(
        grid: PrettyGrid<V>,
        listProperty: KProperty1<T, List<V>>,
        gridID: GridID? = null,
        noinline builderAction: (ValueGridBuilder<T, V>.() -> Unit)? = null
    ): PrettyValueGrid<T, V> {
        val valueType = tokenOf<V>()
        val gridCopy = grid.copy()
        val callable = listProperty.toCallable(receiverType)
        val valueGridBuilder = ValueGridBuilder(property =  callable, gridID)
        valueGridBuilder.addRowsChecking(valueType,  gridCopy.rows)
        builderAction?.invoke(valueGridBuilder)
        return valueGridBuilder.finalizeGrid(prettyGrid)
    }


    @PrettyDSL
    fun <V> useTemplate(
        container: ValueRowBuilder<T, V>,
        gridID: GridID? = null,
        opts: CommonRowOptions? = null,
        builderAction: (ValueRowBuilder<T, V>.() -> Unit)? = null
    ): PrettyValueRow<T, V> {
        container.applyOptions(opts)
        builderAction?.invoke(container)
        val valueGrid = container.finalizeRow(this)
        prettyGrid.renderPlan.add(valueGrid)
        return valueGrid
    }

    @PrettyDSL
    inline fun <reified V: Any> useRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        gridID: GridID? = null,
        noinline builderAction: (ValueGridBuilder<T, V>.() -> Unit)? = null
    ):PrettyValueGrid<T, V>{
        val provider = property.toElementProvider(receiverType)
        val valueGridBuilder = ValueGridBuilder(provider, gridID)
        valueGridBuilder.addRow(row.copy())
        builderAction?.invoke(valueGridBuilder)
        return  valueGridBuilder.finalizeGrid(prettyGrid)
    }

    @PrettyDSL
    @JvmName("useRowList")
    inline fun <reified V: Any> useRow(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        gridID: GridID? = null,
        noinline builderAction: (ValueGridBuilder<T, V>.() -> Unit)? = null
    ):PrettyValueGrid<T, V>{
        val property =  property.toCallable(receiverType)
        val valueGridBuilder = ValueGridBuilder(property =  property, gridID)
        valueGridBuilder.addRow(row.copy())
        builderAction?.invoke(valueGridBuilder)
        return  valueGridBuilder.finalizeGrid(prettyGrid)
    }

    companion object {
        inline operator fun <reified T> invoke(
            gridID: GridID? = null
        ): GridBuilder<T> = GridBuilder(TypeToken<T>(), gridID)
   }
}

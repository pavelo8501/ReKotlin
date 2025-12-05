package po.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.styles.Colour
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


abstract class RowContainerBase<T: Any, V: Any>(
    val typeToken: TypeToken<T>,
    val options: RowOptions
): TokenFactory


class RowContainer<T: Any>(
   typeToken: TypeToken<T>,
   options: RowOptions
): RowContainerBase<T, T>(typeToken, options){

    val prettyGrid: PrettyGrid<T> = PrettyGrid(typeToken)

    @PublishedApi
    internal fun buildGrid(buildr: RowContainer<T>.()-> Unit):PrettyGrid<T>{
        buildr.invoke(this)
        return prettyGrid
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: CellContainer<T>.() -> Unit
    ): Unit {
       val options = PrettyHelper.toRowOptionsOrDefault(rowOptions, options)
       val cellContainer = CellContainer(typeToken, options)
       val row =  cellContainer.buildRow(builder)
        prettyGrid.addRow(row)
    }

    fun <V: Any> useTemplate(
        valueGrid: PrettyValueGrid<T, V>,
        property: KProperty1<T, V>
    ): Unit {
        valueGrid.singleLoader.setReadOnlyProperty(property)
        prettyGrid.addRenderBlock(valueGrid)
    }

    fun <V: Any> useListTemplate(
        valueGrid: PrettyValueGrid<T, V>,
        property: KProperty1<T, List<V>>
    ): Unit {
        valueGrid.listLoader.setReadOnlyProperty(property)
        prettyGrid.addRenderBlock(valueGrid)
    }

    inline fun <reified V: Any> useTemplate(
        valueGrid: PrettyGrid<V>,
        property: KProperty1<T, List<V>>
    ): Unit {
        val container = RowValueContainer(typeToken, valueGrid.typeToken, valueGrid.options)
        container.setProperty(property)
        prettyGrid.addRenderBlock(container.valueGrid)
    }

}

class RowValueContainer<T: Any, V: Any>(
    typeToken: TypeToken<T>,
    val valueToken: TypeToken<V>,
    options: RowOptions
): RowContainerBase<T, V>(typeToken, options){

    val valueGrid: PrettyValueGrid<T, V> = PrettyValueGrid(typeToken, valueToken)

    @PublishedApi
    internal fun buildGrid(
        property: KProperty1<T, V>,
        buildr: RowValueContainer<T, V>.()-> Unit
    ):PrettyValueGrid<T,V>{
        buildr.invoke(this)
        valueGrid.singleLoader.setReadOnlyProperty(property)
        return valueGrid
    }

    @PublishedApi
    @JvmName("buildGridList")
    internal fun buildGrid(
        property: KProperty1<T, List<V>>,
        buildr: RowValueContainer<T, V>.()-> Unit
    ):PrettyValueGrid<T,V>{
        buildr.invoke(this)
        valueGrid.listLoader.setReadOnlyProperty(property)
        return valueGrid
    }

    fun setProperty(
        property: KProperty1<T, List<V>>
    ):PrettyValueGrid<T,V>{
        valueGrid.listLoader.setReadOnlyProperty(property)
        return valueGrid
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: CellReceiverContainer<T, V>.() -> Unit
    ): Unit {
        val cellContainer = CellReceiverContainer<T, V>(typeToken, valueToken,   rowOptions)
        val row =  cellContainer.buildRow(builder)
        valueGrid.addRow(row)
    }

}





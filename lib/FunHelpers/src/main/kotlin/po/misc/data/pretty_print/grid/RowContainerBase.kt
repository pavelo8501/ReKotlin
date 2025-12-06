package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.pretty_print.rows.buildPrettyRow
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

    constructor(grid: PrettyGrid<T>):this(grid.typeToken, grid.options){
        grid.rows.forEach {
            prettyGrid.addRow(it)
        }
    }

    val prettyGrid: PrettyGrid<T> = PrettyGrid(typeToken)

    @PublishedApi
    internal fun buildGrid(buildr: RowContainer<T>.()-> Unit):PrettyGrid<T>{
        buildr.invoke(this)
        return prettyGrid
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: CellContainer<T>.() -> Unit
    ){
       val row = buildPrettyRow(typeToken, rowOptions, builder)
       prettyGrid.addRow(row)
    }

    inline fun <reified V: Any> buildRow(
        property: KProperty1<T, V>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: CellReceiverContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val container =  CellReceiverContainer(typeToken, tokenOf<V>(), options)
        val valueGrid = container.buildGrid(property, builder)
        prettyGrid.addRenderBlock(valueGrid)
    }

    inline fun <reified V: Any> buildRowList(
        property: KProperty1<T, List<V>>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: CellReceiverContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val container =  CellReceiverContainer(typeToken,  tokenOf<V>(), options)
        val valueGrid = container.buildGrid(property, builder)
        prettyGrid.addRenderBlock(valueGrid)
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
        property: KProperty1<T, V>
    ) {
        val container = RowValueContainer(typeToken, valueGrid.typeToken, valueGrid.options)
        val valueGrid = container.initializeByGrid(property, valueGrid)
        prettyGrid.addRenderBlock(valueGrid)
    }

    inline fun <reified V: Any> useListTemplate(
        valueGrid: PrettyGrid<V>,
        property: KProperty1<T, List<V>>
    ){
        val container = RowValueContainer(typeToken, valueGrid.typeToken, valueGrid.options)
        val valueGrid = container.initializeByGrid(property, valueGrid)
        prettyGrid.addRenderBlock(valueGrid)
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
        setListReadOnlyProperty(property)
        return valueGrid
    }


    @PublishedApi
    internal fun buildGrid(
        provider: ()-> V,
        buildr: RowValueContainer<T, V>. (ValueLoader<T, V>)-> Unit
    ):PrettyValueGrid<T,V>{
        valueGrid.singleLoader.setProvider(provider)
        buildr.invoke(this, valueGrid.singleLoader)
        return valueGrid
    }

    fun setReadOnlyProperty(
        property: KProperty1<T, V>,
    ){
        valueGrid.singleLoader.setReadOnlyProperty(property)
    }

    fun setListReadOnlyProperty(
        property: KProperty1<T, List<V>>,
    ){
        valueGrid.listLoader.setReadOnlyProperty(property)
    }

    fun initializeByGrid(
        property: KProperty1<T, V>,
        grid: PrettyGrid<V>
    ): PrettyValueGrid<T, V>{
        setReadOnlyProperty(property)
        grid.rows.forEach {
            valueGrid.addRow(it)
        }
        return valueGrid
    }

    @JvmName("initializeByGridList")
    fun initializeByGrid(
        property: KProperty1<T, List<V>>,
        grid: PrettyGrid<V>
    ): PrettyValueGrid<T, V>{
        setListReadOnlyProperty(property)
        grid.rows.forEach {
            valueGrid.addRow(it)
        }
        return valueGrid
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: CellReceiverContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val cellContainer = CellReceiverContainer<T, V>(typeToken, valueToken, options)
        val row =  cellContainer.buildRow(builder)
        valueGrid.addRow(row)
    }

}





package po.misc.data.pretty_print.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.pretty_print.rows.copyRow
import po.misc.data.pretty_print.rows.createRowContainer
import po.misc.data.pretty_print.rows.createRowValueContainer
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.types.castOrThrow
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


abstract class GridContainerBase<T: Any, V: Any>(
    val hostTypeToken: TypeToken<T>,
    val typeToken: TypeToken<V>,
): TokenFactory{

   // abstract val prettyGridBase: PrettyGridBase<T, V>
   // val options: RowOptions get() = prettyGridBase.options


    val singleLoader: ValueLoader<T, V> = ValueLoader("GridContainerBase", hostTypeToken, typeToken)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("GridContainerBase", hostTypeToken, typeToken)

    internal val gridMap: MutableMap<GridKey, PrettyGrid<*>> = mutableMapOf()
    protected val rowsBacking: MutableList<PrettyRow<V>> = mutableListOf()
    protected var renderMapBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
    val renderBlocks: List<RenderableElement<T>>  get() = renderMapBacking.values.toList()

    val rows: List<PrettyRow<V>> get() = rowsBacking
    val rowsSize : Int get() = rows.size

    val gridsCount: Int get() = gridMap.size
    val renderCount: Int get() = renderMapBacking.size
    val size: Int get() = gridsCount + renderCount + rowsSize

    @PublishedApi
    internal fun insertRenderable(element: RenderableElement<T>, key: GridKey){
        val entries  = renderMapBacking.entries
        val newMap: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
        val indexOfSame = entries.indexOfFirst { it.key.order ==  key.order}
        var sameOrder = entries.toList().getOrNull(indexOfSame)?.key?.order?:0

        entries.take(sameOrder).forEach {
            newMap.entries.add(it)
        }
        newMap[key] = element
        for(entry in entries.drop(sameOrder)){
            sameOrder += 1
            val newKey = GridKey(order = sameOrder,  entry.key.source)
            newMap[newKey] = entry.value
        }
        renderMapBacking = newMap
    }
    @PublishedApi
    internal fun addRenderBlock(newRenderBlock: RenderableElement<T>):GridKey{
        val key =  GridKey(size, GridSource.Renderable)
        renderMapBacking[key] = newRenderBlock
        return key
    }
    @PublishedApi
    internal fun <T2: Any> addGridBlock(newRenderBlock: PrettyGrid<T2>):GridKey{
        val key =  GridKey(size, GridSource.Grid)
        gridMap[key] = newRenderBlock
        return key
    }

    @PublishedApi
    internal open fun addRow(row: PrettyRow<V>): GridKey? {
        rowsBacking.add(row)
        return null
    }
    @PublishedApi
    internal fun addRows(rows: List<PrettyRow<V>>){
        rowsBacking.addAll(rows)
    }
    @PublishedApi
    internal fun insertRows(index: Int,  rows: List<PrettyRow<V>>){
        rowsBacking.addAll(index, rows)
    }

    protected val valueResolved: Signal<V, Unit> = signalOf(typeToken, NoResult)
    protected val resolved: Signal<ValueLoader<T, V>, Unit> = signalOf()
    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf()
    protected val beforeGridRender: Signal<GridParams<T, V>, Unit> = signalOf()
    protected val templateResolved: Signal<PrettyValueGrid<T, *>, Unit> = signalOf()

    fun onValueResolved(callback: (V)-> Unit): Unit = valueResolved.onSignal(callback)
    fun onResolved(callback: (ValueLoader<T, V>)-> Unit): Unit = resolved.onSignal(callback)
    fun beforeRowRender(callback: (RowParams<V>) -> Unit): Unit = beforeRowRender.onSignal(callback)
    fun beforeGridRender(callback: (GridParams<T, V>) -> Unit): Unit =  beforeGridRender.onSignal(callback)
    fun onTemplateResolved(callback: (PrettyValueGrid<T, *>) -> Unit): Unit =  templateResolved.onSignal(callback)

    fun createRow(opts: CommonRowOptions? = null, cells: List<PrettyCellBase> = emptyList()):PrettyRow<V>{
        val options = PrettyHelper.toRowOptions(opts)
        val row = PrettyRow(typeToken, options)
        row.setCells(cells)
        addRow(row)
        return row
    }

    open fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptionsOrNull(rowOptions)
        options?.setNoEdit()

        val container = createRowContainer(typeToken, options)
        val row =  container.applyBuilder(builder)
        addRow(row)
    }

    fun buildRow(
        valueLoader: ValueLoader<T, V>,
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<V>.(V) -> Unit
    ){
        val container = createRowContainer(typeToken, rowOptions)
        val row =  container.applyParametrizedBuilder(valueLoader.getValue(Throwing),  builder)
        addRow(row)
    }

}


class GridContainer<T: Any>(
   typeToken: TypeToken<T>,
   val options: RowOptions? = null
): GridContainerBase<T, T>(typeToken, typeToken){

    fun createGrid(options: RowOptions? = null):PrettyGrid<T>{
       return if(options != null){
            PrettyGrid(typeToken, options)
        }else{
            PrettyGrid(typeToken)
        }
    }

    @PublishedApi
    internal fun applyBuilder(buildr: GridContainer<T>.()-> Unit):PrettyGrid<T>{
        buildr.invoke(this)
        val prettyGrid: PrettyGrid<T> = createGrid(options)
        renderMapBacking.entries.forEach {
            prettyGrid.renderMapBacking[it.key] = it.value
        }
        gridMap.entries.forEach {
            prettyGrid.gridMap[it.key] = it.value
        }
        prettyGrid.addRows(rows)
        return prettyGrid
    }

    @PublishedApi
    internal fun applyBuilder(
        provider: ()-> T,
        builder: GridContainer<T>. ()-> Unit
    ):PrettyGrid<T>{
        val prettyGrid = applyBuilder(builder)
        prettyGrid.singleLoader.setProvider(provider)
        return prettyGrid
    }

    @PublishedApi
    @JvmName("applyBuilderList")
    internal fun applyBuilder(
        provider: ()-> List<T>,
        builder: GridContainer<T>. ()-> Unit
    ):PrettyGrid<T>{
        val prettyGrid = applyBuilder(builder)
        prettyGrid.listLoader.setProvider(provider)
        return prettyGrid
    }

    override fun addRow(row: PrettyRow<T>): GridKey{
        rowsBacking.add(row)
        return  addRenderBlock(row)
    }

    inline fun <reified V: Any> buildRow(
        property: KProperty1<T, V>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val container = RowValueContainer(hostTypeToken, TypeToken.create<V>(), options)
        val valueGrid =  container.buildGrid(property, builder)
        addRenderBlock(valueGrid)
    }

    inline fun <reified V: Any> buildRowList(
        property: KProperty1<T, List<V>>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val container =  RowValueContainer(typeToken,  tokenOf<V>(), options)
        val valueGrid = container.buildGrid(property, builder)
        addRenderBlock(valueGrid)
    }

    fun useTemplate(row: PrettyRow<T>){
        val converted = row.copyRow(typeToken)
        addRow(converted)
    }
    fun useTemplate(grid: PrettyGrid<T>){
        val converted = grid.rows.map { it.copyRow(typeToken) }
        addRows(converted)
    }

    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGrid(typeToken,  property, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val casted = grid.castOrThrow<PrettyValueGrid<T, V>>()
                casted.singleLoader.setReadOnlyProperty(property)
                addRenderBlock(casted)
                return casted
            }
        }
    }

    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        provider: ()-> V
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGrid(typeToken,  provider, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.singleLoader.setProvider(provider)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    fun <V: Any> useListTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, List<V>>,
        orientation: Orientation = grid.options.orientation
    ): PrettyValueGrid<T, V>{
        val optionCopy = grid.options.copy(noEdit = true)
        optionCopy.orientation = orientation
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGridList(typeToken, property, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setReadOnlyProperty(property)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    fun <V: Any> useListTemplate(
        grid: PrettyGridBase<*, V>,
        orientation: Orientation = grid.options.orientation,
        provider: () -> List<V>
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        optionCopy.orientation = orientation

        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGridList(typeToken, provider, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setProvider(provider)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    fun <V: Any> useListTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
    ) {
       val copy = row.options.copy()
       val grid = PrettyValueGrid(hostTypeToken,  row.typeToken, copy)
       grid.listLoader.setReadOnlyProperty(property)
       addRenderBlock(grid)
    }

    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {

        val valueContainer = GridValueContainer(hostTypeToken, grid)
        val container = TemplateGridContainer(hostTypeToken,  valueContainer)
        val valueGrid =   container.applyBuilder(property, builder)
        addRenderBlock(valueGrid)
        templateResolved.trigger(valueGrid)
        return valueGrid
    }

    fun <V: Any> useTemplateList(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, List<V>>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {

        val valueContainer = GridValueContainer(hostTypeToken, grid)
        val container = TemplateGridContainer(hostTypeToken,  valueContainer)
        val valueGrid =  container.applyBuilder(property, builder)
        addRenderBlock(valueGrid)
        templateResolved.trigger(valueGrid)
        return valueGrid
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        val valueContainer = GridValueContainer(hostTypeToken, row.typeToken)
        valueContainer.addRow(row)
        val container = TemplateGridContainer(hostTypeToken, valueContainer)
        val valueGrid = container.applyBuilder(property, builder)
        addRenderBlock(valueGrid)
        templateResolved.trigger(valueGrid)
        return valueGrid
    }

    fun <V: Any> useTemplate(
        rowContainer: RowContainer<V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyGrid<V> {
        val gridContainer = GridValueContainer(hostTypeToken, rowContainer)
        val container = TemplateGridContainer(hostTypeToken, gridContainer)
        val grid = container.buildGrid(builder)
        addGridBlock(grid)
        return grid
    }
}





package po.misc.data.pretty_print.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.copyRow
import po.misc.data.pretty_print.rows.createRowContainer
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

sealed class GridContainerBase<T: Any, V: Any>(
    val hostType: TypeToken<T>,
    val type: TypeToken<V>,
): TokenFactory, TraceableContext{



    val singleLoader: ValueLoader<T, V> = ValueLoader("GridContainerBase", hostType, type)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("GridContainerBase", hostType, type)


    internal val gridMap: MutableMap<GridKey, PrettyGrid<*>> = mutableMapOf()

    protected val rowsBacking: MutableList<PrettyRow<V>> = mutableListOf()
    protected var renderMapBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
    val rows: List<PrettyRow<V>> get() = rowsBacking
    val rowsSize : Int get() = rows.size

    val gridsCount: Int get() = gridMap.size
    val renderCount: Int get() = renderMapBacking.size
    val size: Int get() = gridsCount + renderCount + rowsSize


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

    fun addContainer(rowContainer: RowContainer<V>){
        singleLoader.initValueFrom(rowContainer.singleLoader)
        listLoader.initValueFrom(rowContainer.listLoader)
        addRow(rowContainer.createRow())
    }

    open fun addRow(row: PrettyRow<V>): GridKey?{
        rowsBacking.add(row)
        return null
    }

    @PublishedApi
    internal fun addRows(rows: List<PrettyRow<V>>){
        rows.forEach { row ->
            addRow(row)
        }
    }

    @PublishedApi
    internal fun setProperty(property: KProperty1<T, V>){

        singleLoader.setProperty(property)
    }

    @PublishedApi
    @JvmName("setPropertyList")
    internal  fun setProperty(property: KProperty1<T, List<V>>){
        listLoader.setProperty(property)
    }

    protected val valueResolved: Signal<V, Unit> = signalOf(type, NoResult)
    protected val resolved: Signal<ValueLoader<T, V>, Unit> = signalOf()
    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf()
    protected val beforeGridRender: Signal<GridParams<T, V>, Unit> = signalOf()
    protected val templateResolved: Signal<PrettyValueGrid<T, *>, Unit> = signalOf()

    fun onValueResolved(callback: (V)-> Unit): Unit = valueResolved.onSignal(callback)
    fun onResolved(callback: (ValueLoader<T, V>)-> Unit): Unit = resolved.onSignal(callback)
    fun beforeRowRender(callback: (RowParams<V>) -> Unit): Unit = beforeRowRender.onSignal(callback)
    fun beforeGridRender(callback: (GridParams<T, V>) -> Unit): Unit =  beforeGridRender.onSignal(callback)
    fun onTemplateResolved(callback: (PrettyValueGrid<T, *>) -> Unit): Unit =  templateResolved.onSignal(callback)

    fun setProviders(
        provider: (()-> V)? = null,
        listProvider: (()-> List<V>)? = null,
    ){
        if(provider != null){
            singleLoader.setProvider(provider)
        }
        if(listProvider != null){
            listLoader.setProvider(listProvider)
        }
    }

    fun useTemplate(row: PrettyRow<V>){
        addRow(row)
    }

    fun useTemplate(grid: PrettyGrid<V>){
        val converted = grid.rows.map { it.copyRow(type) }
        addRows(converted)
    }

    open fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptionsOrNull(rowOptions)
        options?.noEdit()
        val container = createRowContainer(type, options)
        val row =  container.applyBuilder(builder)
        addRow(row)
    }

    fun buildRow(
        valueLoader: ValueLoader<T, V>,
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<V>.(V) -> Unit
    ){
        val container = createRowContainer(type, rowOptions)
        val row =  container.applyParametrizedBuilder(valueLoader.getValue(Throwing),  builder)
        addRow(row)
    }
}
package po.misc.data.pretty_print.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.grid.GridKey
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.grid.RenderableMap
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.copyRow
import po.misc.functions.LambdaOptions
import po.misc.properties.checkType
import po.misc.properties.isReturnTypeList
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.asList
import kotlin.reflect.KProperty1

sealed class GridContainerBase<T: Any, V: Any>(
    val hostType: TypeToken<T>,
    val type: TypeToken<V>,
): TokenFactory, TraceableContext{


    protected abstract val grid : PrettyGridBase<T, V>

    val singleLoader: ValueLoader<T, V> get() = grid.singleLoader
    val listLoader: ListValueLoader<T, V> get() = grid.listLoader


  //  internal val gridMap: MutableMap<GridKey, PrettyGrid<*>> = mutableMapOf()
   // protected val rowsBacking: MutableList<PrettyRow<V>> = mutableListOf()
    //protected var renderMapBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
    abstract val rows: List<PrettyRow<V>>
   // val gridsCount: Int get() = gridMap.size
   // val renderCount: Int get() = renderMapBacking.size
    val rowSize: Int get() = rows.size


//    @PublishedApi
//    internal fun addRenderBlock(newRenderBlock: RenderableElement<T>):GridKey{
//        val key =  GridKey(size, GridSource.Renderable)
//        renderMapBacking[key] = newRenderBlock
//        return key
//    }

//    @PublishedApi
//    internal fun <T2: Any> addGridBlock(newRenderBlock: PrettyGrid<T2>):GridKey{
//        val key =  GridKey(size, GridSource.Grid)
//        gridMap[key] = newRenderBlock
//        return key
//    }

    abstract fun addRow(row: PrettyRow<V>): GridKey?

    @PublishedApi
    internal fun addRows(rows: List<PrettyRow<V>>){
        rows.forEach { row ->
            addRow(row)
        }
    }

    @PublishedApi
    @JvmName("setPropertyList")
    internal fun setProperty(property: KProperty1<T, List<V>>){
        listLoader.setProperty(property)
    }

    protected fun makeThrow(message: String): Nothing{
        throw IllegalStateException(message)
    }

    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf()
    protected val beforeGridRender: Signal<GridParams, Unit> = signalOf()
    protected val templateResolved: Signal<PrettyValueGrid<T, *>, Unit> = signalOf()

    fun onResolved(callback: V.(Unit)-> Unit): Unit{
        singleLoader.valueResolved.onSignal(LambdaOptions.Promise, callback = callback)
    }

    fun beforeRowRender(callback: (RowParams<V>) -> Unit): Unit = beforeRowRender.onSignal(callback)
    fun beforeGridRender(callback: (GridParams) -> Unit): Unit =  beforeGridRender.onSignal(callback)
    fun onTemplateResolved(callback: (PrettyValueGrid<T, *>) -> Unit): Unit =  templateResolved.onSignal(callback)

    fun setProperty(property: KProperty1<T, V>){
        if(type.isCollection){
            if(!property.isReturnTypeList){
                makeThrow("Something went wrong. Type token isCollection = true but property isReturnTypeList = false")
            }
            property.checkType(hostType, type.asList())?.let {
                listLoader.setProperty(it)
            }?:run {
                makeThrow("Failed to resolve property to KProperty<T, List<V>")
            }
        }else{
            singleLoader.setProperty(property)
        }
    }

    fun setProviders(
        provider: (()-> V)? = null,
        listProvider: (()-> List<V>)? = null,
    ): GridContainerBase<T, V>{
        if(provider != null){
            singleLoader.setProvider(provider)
        }
        if(listProvider != null){
            listLoader.setProvider(listProvider)
        }
        return this
    }

    fun useTemplate(row: PrettyRow<V>){
        addRow(row)
    }

    fun useTemplate(grid: PrettyGrid<V>){
        val converted = grid.rows.map { it.copyRow(type) }
        addRows(converted)
    }

}
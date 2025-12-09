package po.misc.data.pretty_print.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.addNotBlank
import po.misc.data.output.output
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.createPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.functions.Throwing
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken

sealed class PrettyGridBase<T: Any, V: Any>(
    val hostTypeToken: TypeToken<T>,
    val typeToken :TypeToken<V>,
    var options: RowOptions,
): TokenFactory
{

    internal val gridMap: MutableMap<GridKey, PrettyGrid<*>> = mutableMapOf()
    protected val rowsBacking: MutableList<PrettyRow<V>> = mutableListOf()
    internal var renderMapBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
    val renderBlocks: List<RenderableElement<T>>  get() = renderMapBacking.values.toList()

    val gridsCount: Int get() = gridMap.size
    val renderCount: Int get() = renderMapBacking.size
    val size: Int get() = gridsCount + renderCount + rowsSize

    val id: Enum<*>? get() = options.rowId
    val rows: List<PrettyRow<V>> get() = rowsBacking
    val rowsSize : Int get() = rows.size

    val singleLoader: ValueLoader<T, V> = ValueLoader("ReceiverGrid", hostTypeToken, typeToken)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("ReceiverGrid", hostTypeToken, typeToken)

    val beforeGridRender: Signal<GridParams<T, V>, Unit> = signalOf<GridParams<T,V>, Unit>()
    val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf<RowParams<V>, Unit>()

    protected fun checkShouldRender(row: RenderableElement<*>, opt: RowOptions?): Boolean {

        val useOptions = opt?:options
        if(row.id == null  && useOptions.renderUnnamed){
            return true
        }
        if(row.id == null  && !useOptions.renderUnnamed){
            return false
        }
        if(useOptions.excludeFromRenderList.isNotEmpty()){
           val contains =  row.id in useOptions.excludeFromRenderList
           return !contains
        }
        if (useOptions.renderOnlyList.isNotEmpty()){
            val contains =  row.id in useOptions.renderOnlyList
            return contains
        }
        return true
    }

    open fun addRow(row: PrettyRow<V>):PrettyGridBase<T, V>{
        rowsBacking.add(row)
        return this
    }
    open fun addRows(rows: List<PrettyRow<V>>):PrettyGridBase<T, V>{
        rows.forEach { addRow(it) }
        return this
    }

    fun insertRows(position: Int,   rows: List<PrettyRow<V>>):PrettyGridBase<T, V>{
        rowsBacking.addAll(position, rows)
        return this
    }
    fun getRow(rowId: Enum<*>): PrettyRow<V>{
        return rows.first { rowId == it.id }
    }
    fun getRowOrNull(rowId: Enum<*>): PrettyRow<V>?{
        return rows.firstOrNull{ rowId == it.id }
    }

    fun applyOptions(opts: RowOptions?){
        opts?.let {
            options = it
        }
    }
}

class PrettyGrid<T: Any>(
    typeToken :TypeToken<T>,
    options: CommonRowOptions,
) : PrettyGridBase<T, T>(typeToken, typeToken, PrettyHelper.toRowOptions(options))
{
    constructor(typeToken :TypeToken<T>):this(typeToken, RowOptions())

    val renderMap : Map<GridKey, RenderableElement<T>> get() = renderMapBacking

    private fun renderGrid(grid: PrettyGrid<*>?, opts: CommonRowOptions?): String{
        if(grid != null){
            return grid.render(opts)
        }
        return ""
    }

    private fun renderElement(
        element:  RenderableElement<T>,
        receiver:T,
        opts: RowOptions?,
        optionBuilder: (RowOptions.()-> Unit)?
    ): String{
        val useOptions = if(element.options.useNoEdit){
            element.options
        }else{
            opts?:options
        }
       optionBuilder?.invoke(useOptions)
       beforeGridRender.trigger(GridParams(this, useOptions))
       return  when (element) {
            is PrettyRow<*> -> element.renderOnHost(receiver, useOptions)
            is PrettyValueGrid<T, *> -> element.renderOnHost(receiver, useOptions)
            else ->{
                val notSupportedText = "${receiver::class} not supported any more"
                notSupportedText.output(Colour.Yellow)
                SpecialChars.EMPTY
            }
        }
    }

    fun render(receiver: T, opts: CommonRowOptions? = null, optionBuilder: (RowOptions.()-> Unit)? = null): String {
        val resultList = mutableListOf<String>()
//        val useOptions = PrettyHelper.toRowOptions(opts, options)
//        optionBuilder?.invoke(useOptions)
        val rowOptions = PrettyHelper.toRowOptionsOrNull(opts)
        val keys = (renderMap.keys + gridMap.keys).sortedBy{  it.order }
        for (gridKey in keys) {
            when(gridKey.source){
                GridSource.Grid -> {
                    val render = renderGrid(gridMap[gridKey], rowOptions)
                    resultList.addNotBlank(render)
                }
                GridSource.Renderable -> {
                    val renderBlock = renderMap.getValue(gridKey)
                    val shouldRender = checkShouldRender(renderBlock, rowOptions)
                    if (!shouldRender) continue
                    val render = renderElement(renderBlock, receiver, rowOptions, optionBuilder)
                    resultList.addNotBlank(render)
                }
            }
        }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(receiverList: List<T>, opts: CommonRowOptions? = null, optionBuilder: (RowOptions.()-> Unit)? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        optionBuilder?.invoke(useOptions)
        val resultList =  receiverList.map { render(it, useOptions) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(opts: CommonRowOptions? = null, optionBuilder: (RowOptions.()-> Unit)? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        optionBuilder?.invoke(useOptions)
        beforeGridRender.trigger(GridParams(this, useOptions))
        val value = singleLoader.resolveProvider()
        if(value != null){
            return render(value, useOptions)
        }
        return ""
    }

    companion object

}

class PrettyValueGrid<T: Any, V: Any>(
    hostTypeToken :TypeToken<T>,
    typeToken: TypeToken<V>,
    options: CommonRowOptions = RowOptions(),
) : PrettyGridBase<T, V>(hostTypeToken,  typeToken, PrettyHelper.toRowOptions(options)), RenderableElement<T>
{

    fun render(receiver: V, opts: CommonRowOptions? = null, optionsBuilder: (RowOptions.()-> Unit)? = null): String {
        val resultList = mutableListOf<String>()
        PrettyHelper.toOptionsOrNull(opts)
        val useOptions = PrettyHelper.toRowOptions(opts, options)

        optionsBuilder?.invoke(useOptions)
        beforeGridRender.trigger(GridParams(this, useOptions))
        for (row in rows) {
            val shouldRender = checkShouldRender(row, useOptions)
            if (!shouldRender) continue
            beforeRowRender.trigger(RowParams(row, useOptions))
            val render = row.render(receiver, useOptions)
            resultList.add(render)
        }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(receiverList: List<V>, opts: CommonRowOptions? = null, optionsBuilder: (RowOptions.()-> Unit)? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        optionsBuilder?.invoke(useOptions)
        beforeGridRender.trigger(GridParams(this, useOptions))
        val resultList = receiverList.map { render(it, useOptions) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    @JvmName("renderReceiverHosting")
    fun render(receiver: T, opts: CommonRowOptions? = null, optionsBuilder: (RowOptions.()-> Unit)? = null): String {
        if(singleLoader.canLoadValue){
            val value = singleLoader.resolveValue(receiver, Throwing)
            return  render(value, opts, optionsBuilder)
        }
        if(listLoader.canLoadValue){
            val values = listLoader.resolveValue(receiver, Throwing)
            return render(values, opts, optionsBuilder)
        }
        return SpecialChars.EMPTY
    }

    override fun renderOnHost(host: T, opts: CommonRowOptions?): String =
        render(host, opts)

    override fun addRows(rows: List<PrettyRow<V>>): PrettyValueGrid<T, V>{
        rows.forEach { addRow(it) }
        return this
    }

}










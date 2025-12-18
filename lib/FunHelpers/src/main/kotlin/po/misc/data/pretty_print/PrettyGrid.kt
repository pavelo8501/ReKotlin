package po.misc.data.pretty_print

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.addNotBlank
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.strings.appendGroup
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.functions.Throwing
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken

sealed class PrettyGridBase<T: Any, V: Any>(
    val hostType: TypeToken<T>,
    val type :TypeToken<V>,
    var options: RowOptions,
): TokenFactory
{

    internal var rowsBacking: MutableList<PrettyRow<V>> = mutableListOf()
    internal var renderMapBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()
    val renderBlocks: List<RenderableElement<T>>  get() = renderMapBacking.values.toList()
    val renderCount: Int get() = renderMapBacking.size

    val id: RowID? get() = options.rowId
    val rows: List<PrettyRow<V>> get() = rowsBacking

    val rowsSize : Int get() = rows.size
    abstract val size: Int

    val singleLoader: ValueLoader<T, V> = ValueLoader("ReceiverGrid", hostType, type)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("ReceiverGrid", hostType, type)

    val beforeGridRender: Signal<GridParams, Unit> = signalOf<GridParams, Unit>()
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
    fun getRow(rowId: Enum<*>): PrettyRow<V>{
        return rows.first { rowId == it.id }
    }
    fun getRowOrNull(rowId: Enum<*>): PrettyRow<V>?{
        return rows.firstOrNull{ rowId == it.id }
    }

    fun applyOptions(opts: RowOptions?){
        if(opts != null){
            options = opts
            rows.forEach {row->
                if(row.options.orientation != opts.orientation){
                    row.options.orientation = opts.orientation
                }
            }

        }
    }
}

class PrettyGrid<T: Any>(
    typeToken :TypeToken<T>,
    options: CommonRowOptions,
) : PrettyGridBase<T, T>(typeToken, typeToken, PrettyHelper.toRowOptions(options))
{
    constructor(typeToken :TypeToken<T>):this(typeToken, RowOptions())

    internal var gridMap: MutableMap<GridKey, PrettyGrid<*>> = mutableMapOf()
    val renderMap : Map<GridKey, RenderableElement<T>> get() = renderMapBacking

    val gridsCount: Int get() = gridMap.size
    override val size: Int get() = gridsCount + renderCount

    private fun renderGrid(grid: PrettyGrid<*>?, opts: CommonRowOptions?): String{
        if(grid != null){
            beforeGridRender.trigger(GridParams(grid, opts))
            val render = grid.render(opts)
            return render
        }
        return ""
    }

    private fun renderElement(
        element:  RenderableElement<T>,
        receiver:T,
        opts: RowOptions?,
        optionBuilder: (RowOptions.()-> Unit)?
    ): String{
        val useOptions = if(element.options.sealed){
            element.options
        }else{
            opts?:options
        }
       optionBuilder?.invoke(useOptions)
       beforeGridRender.trigger(GridParams(this, useOptions))
       return  when (element) {
            is PrettyRow<*> -> element.renderOnHost(receiver, useOptions)
            is PrettyValueGrid<T, *> -> element.renderOnHost(receiver, useOptions)
        }
    }

    fun render(receiver: T, opts: CommonRowOptions? = null, optionBuilder: (RowOptions.()-> Unit)? = null): String {
        singleLoader.valueResolved.trigger(receiver)
        val resultList = mutableListOf<String>()
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
        val listValue = listLoader.resolveProvider()
        if(listValue != null){
           return render(listValue, useOptions)
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

    override val size: Int get() = renderCount + rowsSize

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

    override fun renderOnHost(host: T, opts: CommonRowOptions?): String = render(host, opts)

    override fun addRows(rows: List<PrettyRow<V>>): PrettyValueGrid<T, V>{
        rows.forEach { addRow(it) }
        return this
    }

    override fun toString(): String {
       return buildString {
            appendGroup("ValueGrid<${hostType.typeName}, ${type.typeName}>[", "]",::id, ::size)
        }
    }

}










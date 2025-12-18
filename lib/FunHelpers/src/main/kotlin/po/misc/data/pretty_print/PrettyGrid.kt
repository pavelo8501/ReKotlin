package po.misc.data.pretty_print

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.addNotBlank
import po.misc.counters.DataRecord
import po.misc.counters.SimpleJournal
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.grid.GridKey
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.grid.RenderableMap
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.grid.RenderableWrapper
import po.misc.data.pretty_print.parts.rows.RowParams
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

    val journal: SimpleJournal = SimpleJournal(this.toString())
    val enabled: Boolean = true

    abstract val size :Int
    abstract val rows: List<PrettyRow<V>>

    val singleLoader: ValueLoader<T, V> = ValueLoader("PrettyGridBase", hostType, type)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("PrettyGridBase", hostType, type)

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

    abstract fun addRow(row: PrettyRow<V>): GridKey?
    open fun addRows(rows: List<PrettyRow<V>>):PrettyGridBase<T, V>{
        rows.forEach { addRow(it) }
        return this
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
) : PrettyGridBase<T, T>(typeToken, typeToken, PrettyHelper.toRowOptions(options)), RenderableElement<T>
{

    constructor(typeToken :TypeToken<T>):this(typeToken, RowOptions())

    val renderMap: RenderableMap<T> = RenderableMap(hostType)
    override val rows: List<PrettyRow<T>> get() = renderMap.rows
    override val size: Int get() = renderMap.size

    private fun renderGrid(grid: PrettyGrid<*>?, opts: CommonRowOptions?): String{
        if(grid != null){
            beforeGridRender.trigger(GridParams(grid, opts))
            val render = grid.render(opts)
            return render
        }
        return ""
    }

    override fun addRow(row: PrettyRow<T>): GridKey{
       return renderMap.addRenderElement(row)
    }

    fun render(receiver: T, opts: CommonRowOptions? = null): String {
        val record : DataRecord = journal.info("render method")
        singleLoader.valueResolved.trigger(receiver)
        singleLoader.notifyResolved(receiver)
        val resultList = mutableListOf<String>()
        val rowOptions = PrettyHelper.toRowOptionsOrNull(opts)
        for (renderable : RenderableWrapper in  renderMap.renderables) {
            val shouldRender = renderable.element.shouldRender()
            if(!shouldRender){
                record.addComment("$renderable skipped. shouldRender : false")
                continue
            }
            when(renderable.type){
                RenderableType.Grid -> {
                    val render = renderable.asGrid<T>().render(receiver, rowOptions)
                    resultList.addNotBlank(render){
                        record.addComment("$renderable returned empty string")
                    }
                }
                RenderableType.ValueGrid -> {
                    val render = renderable.asValueGrid<T>().renderOnHost(receiver, rowOptions)
                    resultList.addNotBlank(render){
                        record.addComment("$renderable returned empty string")
                    }
                }
                RenderableType.Row -> {
                    val render = renderable.asRow<T>().renderOnHost(receiver, rowOptions)
                    resultList.addNotBlank(render){
                        record.addComment("$renderable returned empty string")
                    }
                }
                RenderableType.ForeignGrid  -> {
                    val render = renderGrid(renderable.asForeignGrid(), rowOptions)
                    resultList.addNotBlank(render){
                        record.addComment("renderGrid returned empty string")
                    }
                }
            }
        }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        val resultList =  receiverList.map { render(it, useOptions) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(opts: CommonRowOptions? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
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

    override fun renderOnHost(host: T, opts: CommonRowOptions?): String = render(host, opts)

    override fun toString(): String {
        return buildString {
            val thisId = id
            append("PrettyGrid<${hostType.typeName}, ${type.typeName}>[ id : $thisId")
        }
    }

    companion object
}

class PrettyValueGrid<T: Any, V: Any>(
    hostType :TypeToken<T>,
    type: TypeToken<V>,
    options: CommonRowOptions = RowOptions(),
) : PrettyGridBase<T, V>(hostType,  type, PrettyHelper.toRowOptions(options)), RenderableElement<T>
{

    private val rowsBacking = mutableListOf<PrettyRow<V>>()
    override val rows:List<PrettyRow<V>> = rowsBacking
    override val size: Int get() = rows.size

    fun render(receiver: V, opts: CommonRowOptions? = null, optionsBuilder: (RowOptions.()-> Unit)? = null): String {
        val record = journal.info("render receiver: V method")
        val resultList = mutableListOf<String>()
        PrettyHelper.toOptionsOrNull(opts)
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        optionsBuilder?.invoke(useOptions)
        beforeGridRender.trigger(GridParams(this, useOptions))
        for (row in rows) {
            val shouldRender = row.shouldRender()
            if (!shouldRender){
                record.addComment("$row skipped. shouldRender : false")
                continue
            }
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
        singleLoader.notifyResolved(receiver)
        singleLoader.info().output()
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

    override fun addRow(row: PrettyRow<V>): GridKey?{
        rowsBacking.add(row)
        return null
    }

    override fun addRows(rows: List<PrettyRow<V>>): PrettyValueGrid<T, V>{
        rows.forEach { addRow(it) }
        return this
    }

    override fun toString(): String {
       return buildString {
           val thisId = id
           append("ValueGrid<${hostType.typeName}, ${type.typeName}>[ id : $thisId")
        }
    }

}










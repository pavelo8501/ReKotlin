package po.misc.data.pretty_print

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.asList
import po.misc.counters.DataRecord
import po.misc.counters.SimpleJournal
import po.misc.data.ifNotNull
import po.misc.data.ifNull
import po.misc.data.ifUndefined
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.dsl.RenderConfigurator
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.NamedTemplate
import po.misc.data.pretty_print.parts.template.TemplateData
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.functions.Throwing
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.ifCasted
import po.misc.types.token.safeCast
import po.misc.types.token.tokenOf


class GridSignals<T, V>(
    val grid: PrettyGridBase<T, V>
){
    val beforeGridRender: Signal<GridParams, Unit> = signalOf<GridParams, Unit>()
    val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf<RowParams<V>, Unit>()
}

sealed class PrettyGridBase<T, V>(
    val hostType: TypeToken<T>,
    override val valueType :TypeToken<V>,
    var options: RowOptions,
    var verbosity: Verbosity = Verbosity.Info,
): TokenFactory, PrettyHelper, TemplateHost<T, V>{

    constructor(
        provider: DataProvider<T, V>,
        options: RowOptions,
        verbosity: Verbosity = Verbosity.Info
    ) :this(provider.receiverType, provider.valueType, options, verbosity)

    val journal: SimpleJournal = SimpleJournal(this::class)

    val dataLoader: DataLoader<T, V> = DataLoader("PrettyGridBase", hostType, valueType)

    private var enabledSwitch: Boolean = true
    override val enabled: Boolean get() {
        if(enabledSwitch){
           return dataLoader.canResolve
        }
        return false
    }
    abstract val rows: List<PrettyRow<V>>
    abstract val templateData : TemplateData
    internal val signals = GridSignals(this)

    fun beforeGridRender(callback: (GridParams)->Unit){
        signals.beforeGridRender.onSignal(callback)
    }
    fun beforeRowRender(callback: (RowParams<V>)->Unit){
        signals.beforeRowRender.onSignal(callback)
    }

    abstract fun addRow(row: PrettyRow<V>): PrettyRow<V>
    open fun addRows(rows: List<PrettyRow<V>>): PrettyGridBase<T, V> {
        rows.forEach { addRow(it) }
        return this
    }

    fun resolveList(receiverList: List<T>): List<V>{
        val resultList = dataLoader.resolveList(receiverList)
        ifUndefined(resultList){
            "$this received no values while trying to resolve ${hostType.typeName}"
        }
        return resultList
    }
    fun applyOptions(opt: CommonRowOptions?): PrettyGridBase<T, V>{
        toRowOptionsOrNull(opt)?.let {
            options = it
        }
        return this
    }
    fun renderRow(row: PrettyRow<V>, receiverList: List<V>, opts: CommonRowOptions? = null):String {
        val render = row.render(receiverList, opts?:row.options)
        return render
    }

    internal fun renderMain(renderable: RenderableElement<T, V>, receiverList: List<V>):String {
        val result = mutableListOf<String>()
        receiverList.forEach { receiver ->
            val record = journal.methodNext("renderSafeList")
            val shouldRender = renderable.shouldRender()
            signals.beforeGridRender.trigger(GridParams(this, "renderSafe",  renderable))
            if (!shouldRender) {
                record?.addComment("$renderable skipped. shouldRender : false")
                return SpecialChars.EMPTY
            }
            when (renderable) {
                is PrettyRow<*> -> {
                    val castResult = renderable.ifCasted<PrettyRow<V>, V>(valueType) {
                        val render = render(receiverList)
                        result.add(render)
                    }
                    if (!castResult.success) {
                        castResult.message.output(Colour.YellowBright)
                    }
                }
            }
        }
        return result.joinToString("\n")
    }
}

class PrettyGrid<T>(
    override val receiverType : TypeToken<T>,
    options: RowOptions = RowOptions(Orientation.Horizontal),
    gridID: GridID? = null,
) : PrettyGridBase<T, T>(receiverType, receiverType, options), TemplateHost<T, T>
{

    override val renderPlan: RenderPlan<T, T> = RenderPlan(this)
    override val rows: List<PrettyRow<T>> get() = renderPlan.rows
    override val renderableType: RenderableType = RenderableType.Grid

    override val templateData : TemplateData = PrettyHelper.createGridData(gridID, receiverType, hashCode())
    override val enabled: Boolean  = true
    override val id: NamedTemplate = templateData.templateID

    private val idString: String get() =  buildString {
        append("PrettyGrid<${hostType.typeName},${valueType.typeName}>")
        append("[Id: $id]")
    }
    val size: Int get() = renderPlan.renderMap.size

    init {
        renderPlan.onRender(::renderMain)
    }

    private fun preRenderConfig(configurator: (RenderConfigurator.()-> Unit)?){
        if(configurator != null) {
            val conf = RenderConfigurator()
            configurator.invoke(conf)
            conf.resolveAll(this)
        }
    }

    override fun addRow(row: PrettyRow<T>):PrettyRow<T>{
        renderPlan.add(row)
        return row
    }

    fun render(values: List<T>, renderable: RenderableElement<T, T>): String {
        val render = renderMain(renderable, values)
        return render
    }
    fun render(
        receiver: T,
        opts: CommonRowOptions? = null,
        configurator: (RenderConfigurator.()-> Unit)? = null,
    ): String {
        val record : DataRecord = journal.method("render", "entry point")
        applyOptions(opts)
        preRenderConfig(configurator)
        val render =  renderPlan.renderList(receiver.asList())
        return render
    }

    fun render(opts: CommonRowOptions? = null): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        signals.beforeGridRender.trigger(GridParams(this, useOptions))
        val value = dataLoader.resolveValue()
        if(value != null){
            return render(value, useOptions)
        }
        return renderPlan.renderList(emptyList())
    }
    override fun render(receiverList: List<T>, opts: CommonRowOptions?): String{
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        return  renderPlan.renderList(receiverList, useOptions)
    }
    override fun resolve(receiverList: List<T>): List<T>{
        return receiverList
    }
    override fun copy(usingOptions: CommonRowOptions?): PrettyGrid<T>{
        val thisRowId = id
        val opts = toRowOptionsOrNull(usingOptions)
        val gridCopy =  if(thisRowId is GridID){
            PrettyGrid(receiverType, options.copy(), thisRowId).applyOptions(opts) as  PrettyGrid<T>
        }else{
            PrettyGrid(receiverType,  options.copy()).applyOptions(opts) as  PrettyGrid<T>
        }
        gridCopy.renderPlan.populateBy(renderPlan.copy())
        return gridCopy
    }
    override fun toString(): String = idString

    companion object : TemplateCompanion {
        val sourceColour: Colour = Colour.Blue
        val prettyName : String= "Grid".colorize(sourceColour)

        inline operator fun <reified T> invoke(options: CommonRowOptions? = null) : PrettyGrid<T> {
            PrettyHelper.toRowOptions(options)
            return PrettyGrid(tokenOf<T>(), PrettyHelper.toRowOptions(options))
        }
    }
}

class PrettyValueGrid<T, V>(
    val dataProvider: DataProvider<T, V>,
    options: RowOptions = RowOptions(Orientation.Horizontal),
    gridID: GridID? = null,
) : PrettyGridBase<T, V>(dataProvider.typeToken,  dataProvider.valueType, options), TemplateHost<T, V> {
    override val receiverType: TypeToken<T> = dataLoader.typeToken

    constructor(
        dataProvider: DataProvider<T, V>,
        rowContainer: ValueRowBuilder<T, V>,
        options: CommonRowOptions? = null,
        gridID: GridID? = null,
    ) : this(dataProvider, PrettyHelper.toRowOptions(options), gridID) { addRow(rowContainer.finalizeRow()) }

    constructor(
        dataProvider: DataProvider<T, V>,
        rows: Collection<PrettyRow<V>>,
        options: CommonRowOptions? = null,
        gridID: GridID? = null,
    ) : this(dataProvider, PrettyHelper.toRowOptions(options), gridID) { addRows(rows.toList()) }

    init { dataLoader.applyCallables(dataProvider) }

    override fun resolve(receiverList: List<T>): List<V> = resolveList(receiverList)

    override val templateData : TemplateData = PrettyHelper.createTemplateData(gridID, dataProvider, RenderableType.ValueGrid)
    override val renderPlan: RenderPlan<T, V>  = RenderPlan(this)

    override val rows:List<PrettyRow<V>> = renderPlan.rows
    override val id: NamedTemplate = templateData.templateID
    override val renderableType: RenderableType = RenderableType.ValueGrid

    val size: Int get() = rows.size

    @JvmName("RenderValue")
    fun render(receiver: V, opts: CommonRowOptions? = null): String {
        val record = journal.info("render receiver: V method")
        val resultList = mutableListOf<String>()
        PrettyHelper.toOptionsOrNull(opts)
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        signals.beforeGridRender.trigger(GridParams(this, useOptions))
        for (row in rows) {
            val shouldRender = row.shouldRender()
            if (!shouldRender){
                record.addComment("$row skipped. shouldRender : false")
                continue
            }
            signals.beforeRowRender.trigger(RowParams(row, useOptions))
            val render = row.render(receiver, useOptions)
            resultList.add(render)
        }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }

    @JvmName("RenderList")
    fun render(receiverList: List<V>, opts: CommonRowOptions?): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        signals.beforeGridRender.trigger(GridParams(this, useOptions))
        val resultList = receiverList.map { render(it, useOptions) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }

    fun render(receiver: T, opts: RowOptions): String{
        dataLoader.notifyResolved(receiver)
        val value = dataLoader.resolveList(receiver)
        return renderPlan.renderList(value)
    }

    override fun render(
        receiverList: List<T>,
        opts: CommonRowOptions?
    ): String {
       val results = dataLoader.resolveList(receiverList)
       return renderPlan.renderList(results)
    }

    override fun addRow(row: PrettyRow<V>): PrettyRow<V>{
        renderPlan.add(row)
        return row
    }
    override fun addRows(rows: List<PrettyRow<V>>): PrettyValueGrid<T, V>{
        rows.forEach { addRow(it) }
        return this
    }

    override fun copy(usingOptions: CommonRowOptions? ): PrettyValueGrid<T, V>{
        toRowOptions(usingOptions,  options.copy())
        val thisRowId = id
        val gridCopy =   if(thisRowId is GridID){
            PrettyValueGrid(dataLoader.createDataProvider(), options.copy(), thisRowId)
        }else{
            PrettyValueGrid(dataLoader.createDataProvider(), options.copy())
        }
        val rowsCopy = rows.map { it.copy() }
        gridCopy.addRows(rowsCopy)
        return gridCopy
    }
    override fun equals(other: Any?): Boolean {
        if(other !is PrettyValueGrid<*,*>) return false
        if(other.typeToken != typeToken) return false
        if(other.valueType != valueType) return false
        if(other.dataLoader != dataLoader) return false
        if(other.id != id) return false
        if(other.size != size) return false
        return true
    }
    override fun hashCode(): Int {
        var result = typeToken.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + dataLoader.hashCode()
        result = 31 * result + size
        return result
    }
    override fun toString(): String {
       return buildString {
           val thisId = id
           append("ValueGrid<${hostType.typeName}, ${valueType.typeName}>[ id : $thisId")
        }
    }
    companion object: TemplateCompanion{
        val sourceColour: Colour = Colour.Green
        val prettyName : String= "ValueGrid".colorize(sourceColour)
    }
}

class TransitionGrid<T, V>(
    val provider: DataProvider<T, V>,
    options: RowOptions? = null,
    val gridID: GridID? = null,
): PrettyGridBase<T, V>(provider, options?:defaultOptions), TemplateHost<T, V>
{
    constructor(
        provider: DataProvider<T, V>,
        sourceGrid: PrettyGrid<V>,
        gridID: GridID? = null
    ):this(provider, sourceGrid.options.copy(), gridID){
        val copiedRows = sourceGrid.rows.map { it.copy() }
        addRows(copiedRows)
    }

    override val receiverType:TypeToken<T> = provider.typeToken
    override val renderPlan: RenderPlan<T, V> = RenderPlan(this)
    override val renderableType: RenderableType = RenderableType.Transition

    val size: Int get() = renderPlan.size
    override val rows: List<PrettyRow<V>>  = mutableListOf()

    override val templateData : TemplateData = PrettyHelper.createTemplateData(gridID, provider, RenderableType.Transition)
    override val id: NamedTemplate = templateData.templateID

    init {
        dataLoader.applyCallables(provider)
    }

    override fun resolve(receiverList: List<T>): List<V>{
        val resultList = dataLoader.resolveList(receiverList)
        ifUndefined(resultList){
            "$this received no values while trying to resolve ${receiverType.typeName}"
        }
        return resultList
    }
    fun render(receiver: T, opts: RowOptions): String {
        val values = dataLoader.resolveList(receiver)
        return renderPlan.renderList(values)
    }
    fun render(opts: CommonRowOptions?): String {
        val values =  dataLoader.resolveList()
        return renderPlan.renderList(values, toRowOptions(opts, options))
    }
    override fun render(receiverList: List<T>, opts: CommonRowOptions?): String {
        val useOptions = toRowOptions(opts, options)
        val values = resolveList(receiverList)
        return renderPlan.renderList(values, useOptions)
    }
    internal fun onReceiverResolved(callback: (RenderableElement<T, V>, values: List<V>) -> String){
        ifNotNull(renderPlan.onRender){
            "onReceiverResolved was overwritten by onReceiverResolved method of $this".output(Colour.YellowBright)
        }
        renderPlan.onRender(callback)
    }
    override fun addRow(row: PrettyRow<V>):PrettyRow<V>{
        renderPlan.add(row)
        return row
    }

    override fun copy(usingOptions: CommonRowOptions?): TransitionGrid<T, V> {
        val thisID = id
        return if(thisID is GridID){
            TransitionGrid(provider, options, thisID)
        }else{
            TransitionGrid(provider, options)
        }
    }

    override fun hashCode(): Int {
        var result = receiverType.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + dataLoader.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }
    override fun equals(other: Any?): Boolean {
        if(other !is PrettyValueGrid<*, *>) return false
        if(other.typeToken != receiverType) return false
        if(other.valueType != valueType) return false
        if(other.dataLoader != dataLoader) return false
        if(other.options != options) return false
        return true
    }

    companion object: TemplateCompanion{
        val sourceColour: Colour = Colour.Cyan
        val prettyName : String=  "TransitionGrid".colorize(sourceColour)
        val defaultOptions:RowOptions = RowOptions(Orientation.Vertical)
    }
}










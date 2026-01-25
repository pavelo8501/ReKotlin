package po.misc.data.pretty_print

import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.toCallable
import po.misc.callbacks.validator.ValidityCondition
import po.misc.collections.toList
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.SourceLessCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.common.RenderMarker
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowBuildOption
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.ValueRenderNode
import po.misc.data.pretty_print.parts.rows.BoundRenderNode
import po.misc.data.pretty_print.parts.rows.StaticRenderNode
import po.misc.data.pretty_print.parts.rows.RowRenderPlanner
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.parts.options.TemplateData
import po.misc.data.pretty_print.parts.rendering.KeyRenderParameters
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.error
import po.misc.interfaces.named.NamedComponent
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.String
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

sealed class PrettyRowBase<S, T>(
    val sourceType: TypeToken<S>,
    final override val receiverType: TypeToken<T>,
    initialCells: List<PrettyCellBase<*>>,
    rowID: RowID? = null,
      opts: CommonRowOptions? = null,
):  TraceableContext, PrettyHelper, NamedComponent, TemplatePart<T> {

    val templateData: TemplateData = createRowData(this,  renderableType, rowID)
    override val name: String = templateData.templateID.name
    private var firstRender:Boolean = true
    private val cellsBacking: MutableList<PrettyCellBase<*>> = mutableListOf()

    internal val planner : RowRenderPlanner<T> = RowRenderPlanner(this)
    override var verbosity: Verbosity = Verbosity.Warnings
        set(value) {
            field = value
            planner.verbosity = value
        }

    var options: RowOptions = RowOptions(Orientation.Horizontal)
    set(value) {
        field = value
        keyParameters.initByOptions(value)
    }

    override val keyParameters: KeyRenderParameters get() =  planner.keyParams

    final override val renderableType : RenderableType get() {
        return if(this is PrettyRow<*>){
            RenderableType.Row
        }else{
            RenderableType.ValueRow
        }
    }
    override val templateID: RowID get() =  templateData.templateID as RowID
    val cells : List<PrettyCellBase<*>> get() = cellsBacking
    override var enabled: Boolean = true
    var staticCellsCount: Int = 0
        private set
    var prettyCellsCount: Int = 0
        private set
    var keyedCellsCount: Int = 0
        private set
    var computedCellsCount: Int = 0
        private set

    internal val renderConditions = mutableListOf<ValidityCondition<*>>()
    val size: Int get() = cells.size
    val infoString: String get() = buildString {
        append("$name ")
        append("Static: $staticCellsCount ")
        append("Pretty: $prettyCellsCount ")
        append("Keyed: $keyedCellsCount ")
        append("Computed: $computedCellsCount")
    }
    
    init {
        val opts  = toRowOptionsOrNull(opts)
        if(opts != null){
            options = opts
        }
        initCells(initialCells)
    }
    private fun preRenderConfig(renderHost: RenderParameters?){
        if(cells.isEmpty()){
            notify("No cells. Nothing to render", NotificationTopic.Warning)
            return
        }
        if(firstRender){
            firstRender = false
        }
        if(!planner.isConfigured){
            planner.createRenderNodes(cells, renderHost)
        }
    }
    private fun handlePassedOpts(opts: CommonRowOptions?){
        val passedOptions = toRowOptionsOrNull(opts)
        if(passedOptions != null){
            planner.keyParams.initByOptions(passedOptions)
        }
    }
    private fun renderReturningData(receiver:T, renderHost: RenderParameters?):RenderData{
        preRenderConfig(renderHost)
        for(node in planner.nodes){
            when (node) {
                is StaticRenderNode -> node.render(Unit)
                is BoundRenderNode<*> -> {
                    node.safeCast<BoundRenderNode<T>>()?.render(receiver)
                }
                else -> {}
            }
        }
        return planner.finalizeRender()
    }

    internal fun initCells(cells: List<PrettyCellBase<*>>){
        if(cells.isNotEmpty()){
            cellsBacking.clear()
            cellsBacking.addAll(cells)
            prettyCellsCount  =  cells.filterIsInstance<PrettyCell>().size
            staticCellsCount =  cells.filterIsInstance<StaticCell>().size
            keyedCellsCount  = cells.filterIsInstance<KeyedCell<*>>().size
            computedCellsCount = cells.filterIsInstance<ComputedCell<*, *>>().size
        }
    }

    open fun applyOptions(opt: CommonRowOptions?): PrettyRowBase<S, T>{
        val passedOptions = toRowOptionsOrNull(opt)
        if(passedOptions != null){
            options = passedOptions
            planner.keyParams.initByOptions(passedOptions)
            planner.createRenderNodes(cells)
        }
        return this
    }
    protected fun doRenderStaticOnly(opts: CommonRowOptions?):String{
        handlePassedOpts(opts)
        preRenderConfig(null)
        planner.getStatic().forEach { node->
            node.render(Unit)
        }
        return planner.finalizeRender().styled
    }
    protected fun doRender(values: List<Any>, opts: CommonRowOptions?): String {
        handlePassedOpts(opts)
        preRenderConfig(null)
        if(values.isEmpty()){
            return doRenderStaticOnly(opts)
        }
        var valueIndex = 0
        for(node in planner.nodes){
            when (node) {
                is StaticRenderNode -> node.render(Unit)
                is ValueRenderNode -> {
                    values.getOrNull(valueIndex)?.let {
                        node.render(it)
                        valueIndex ++
                    }
                }
                is BoundRenderNode<*> -> {
                    values.getOrNull(valueIndex)?.let { value ->
                        planner.checkSourceAware(value, node)?.let {checkedPair->
                            checkedPair.second.render(checkedPair.first)
                        }
                        valueIndex ++
                    }
                }
            }
        }
        return planner.finalizeRender().styled
    }
    protected fun doRender(receiver:T, opts: CommonRowOptions?): String {
        handlePassedOpts(opts)
        return renderReturningData(receiver, null).styled
    }
    protected fun doScopedRender(hostParameters: RenderParameters, receiver:T): RenderData {
       return renderReturningData(receiver, hostParameters)
    }
    protected fun doRender(marker: RenderMarker, receiver:T): RenderData {
        return renderReturningData(receiver, null)
    }

    inline fun <reified CT: PrettyCellBase<*>> cellsOf(): List<CT>{
        return cells.filterIsInstance<CT>()
    }
}

class PrettyRow<T>(
    receiverType: TypeToken<T>,
    rowID: RowID? = null,
    opts: CommonRowOptions? = null,
    initialCells: List<PrettyCellBase<*>>,
): PrettyRowBase<T, T>(receiverType, receiverType, initialCells, rowID, opts),  RenderableElement<T, T>, TraceableContext, PrettyHelper{

    override var dataLoader: DataLoader<T, T> = DataLoader("PrettyRow", typeToken, typeToken)
        internal set

    override fun renderFromSource(marker: RenderMarker, source:T, opts: CommonRowOptions? ): RenderData =
        doRender(marker, source)

    override fun renderFromSource(source:T, opts: CommonRowOptions? ): String = doRender(source, opts)

    fun render(receiver:T, optionsBuilder: RowOptions.()-> Unit): String {
        optionsBuilder.invoke(options)
        return doRender(receiver, options)
    }
    fun render(receiverList: List<T>, optionsBuilder: (RowOptions.()-> Unit)): String {
        optionsBuilder.invoke(options)
        val resultList =  receiverList.map { doRender(it, options) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(receiver:T, opts: CommonRowOptions? = null): String{
       return  doRender(receiver, opts)
    }
    fun render(receiverList: List<T>, opts: CommonRowOptions?): String {
        val resultList =  receiverList.map { doRender(it, opts) }
        return resultList.joinToString(SpecialChars.NEW_LINE)
    }

    fun renderAny(vararg values: Any, opts: CommonRowOptions? = null):String {
        val flattened = values.flattenVarargs()
        return doRender(flattened, opts)
    }
    fun render(opts: CommonRowOptions? = null):String {
       return doRenderStaticOnly(opts)
    }

    fun RenderParameters.renderInScope(receiver: T):RenderData{
        return  doScopedRender(this, receiver)
    }

    override fun copy(usingOptions: CommonRowOptions?): PrettyRow<T>{
       return if(usingOptions != null){
           val copiedCells = cells.map { it.copy() }
           PrettyRow(receiverType, templateID,  usingOptions, copiedCells)
        }else{
            val copiedCells = cells.map { it.copy() }
            PrettyRow(receiverType, templateID, options.copy(), copiedCells)
        }
    }
    override fun equals(other: Any?): Boolean {
        if(other !is PrettyRow<*>) return false
        if(other.templateID != templateID) return false
        if(other.receiverType != receiverType) return false
        if(other.cells.size != cells.size) return false
        return true
    }
    override fun hashCode(): Int {
        var result =  (templateID.hashCode())
        result = 31 * result + receiverType.hashCode()
        result = 31 * result + cells.size
        return result
    }
    override fun toString(): String = infoString

    companion object: TemplateCompanion<PrettyRow<*>>, PrettyHelper {

        override val templateClass: KClass<PrettyRow<*>> = PrettyRow::class
        override val renderType: RenderableType = RenderableType.Row
        val sourceColour : Colour = Colour.Magenta
        val prettyName : String= "Row".colorize(sourceColour)

        operator fun invoke(
            rowID: RowID,
            opts: RowOptions? = null,
            firstCell : SourceLessCell,
            vararg cells: SourceLessCell,
        ):PrettyRow<Any> {
            val prettyCells =  cells.toList(firstCell).filterIsInstance<PrettyCellBase<*>>()
            return  PrettyRow(TypeToken<Any>(), rowID, opts,  prettyCells)
        }

        @PublishedApi
        internal fun <T> create(
            token:TypeToken<T>,
            rowID: RowID? = null,
            opts: RowBuildOption? = null,
            prettyCells: List<PrettyCellBase<*>>
        ):PrettyRow<T>{
            val row = when (opts) {
                is RowID -> PrettyRow(token, rowID = opts, initialCells = prettyCells)
                is CommonRowOptions -> PrettyRow(token, rowID = rowID, opts = opts, initialCells =  prettyCells)
                else -> PrettyRow(token, rowID = rowID, opts = null, initialCells = prettyCells)
            }
            return row
        }

        inline operator fun <reified T> invoke(
            rowID: RowID,
            opts: RowBuildOption? = null,
            cells: List<PrettyCellBase<*>> = emptyList()
        ):PrettyRow<T> = create(tokenOf<T>(),rowID, opts, cells)


        @JvmName("PrettyRowAny")
        operator fun invoke(
            rowID: RowID,
            opts: RowOptions? = null,
            cells: List<SourceLessCell>,
        ):PrettyRow<Any> {
            return  PrettyRow(TypeToken<Any>(), rowID, opts, cells.filterIsInstance<PrettyCellBase<*>>())
        }

        @JvmName("PrettyRowAnyList")
        operator fun invoke(
            sourceLessCells: List<SourceLessCell>,
            rowOption: RowBuildOption = RowOptions(Orientation.Horizontal),
        ):PrettyRow<Any> {
            val tokenAny = TypeToken<Any>()
            val prettyCells = sourceLessCells.filterIsInstance<PrettyCellBase<*>>()
            return create(tokenAny, null, rowOption, prettyCells)
        }

        operator fun invoke(
            opts: RowBuildOption,
            vararg sourceLessCells: SourceLessCell,
        ):PrettyRow<Any> {
            val tokenAny = TypeToken<Any>()
            val prettyCells = sourceLessCells.toList().filterIsInstance<PrettyCellBase<*>>()
            return create(tokenAny, null, opts, prettyCells)
        }

        operator fun invoke(
            vararg sourceLessCells: SourceLessCell,
        ):PrettyRow<Any> {
            val tokenAny = TypeToken<Any>()
            val prettyCells = sourceLessCells.toList().filterIsInstance<PrettyCellBase<*>>()
            return create(tokenAny, null, null, prettyCells)
        }

        @JvmName("PrettyRowT")
        inline operator fun <reified T: Any> invoke(
            opts: RowBuildOption? = null,
            cells: List<PrettyCellBase<*>>
        ):PrettyRow<T> {
            val token = tokenOf<T>()
            return  create(token, null,  opts, cells)
        }

        @JvmName("PrettyRowTPrettyCellBase")
        inline operator fun <reified T: Any> invoke(
            vararg cells: PrettyCellBase<*>,
        ):PrettyRow<T> {
            return  create(tokenOf<T>(), null, null, cells.toList())
        }

        operator fun <T: Any> invoke(
            rowID: RowID,
            opts: RowOptions? = null,
            firstCell: SourceAwareCell<T>,
            vararg cells: SourceAwareCell<T>,
        ):PrettyRow<T> {
            return PrettyRow(firstCell.sourceType, rowID, opts, cells.toList(firstCell).filterIsInstance<PrettyCellBase<*>>())
        }

        inline operator fun <reified T: Any> invoke(
            opts: RowBuildOption,
            sourceAwareCells: List<SourceAwareCell<T>>,
        ):PrettyRow<T> {
            val token = tokenOf<T>()
            val prettyCells = sourceAwareCells.filterIsInstance<PrettyCellBase<*>>()
            return  create(token, null,  opts, prettyCells)
        }
        operator fun <T: Any> invoke(
            opts: RowBuildOption,
            sourceAwareCell: SourceAwareCell<T>,
            vararg sourceAwareCells: SourceAwareCell<T>,
        ):PrettyRow<T> {
            val token = sourceAwareCell.sourceType
            val prettyCells = sourceAwareCells.toList(sourceAwareCell).filterIsInstance<PrettyCellBase<*>>()
            return create(token,null,  opts, prettyCells)
        }

        operator fun <T: Any> invoke(
            prettyCell: PrettyCellBase<T>,
            vararg sourceAwareCells: PrettyCellBase<*>,
        ):PrettyRow<T> {
            val token = prettyCell.type
            val prettyCells = sourceAwareCells.toList(prettyCell)
            return create(token, null, null, prettyCells)
        }
    }
}

class PrettyValueRow<S, T>(
    sourceType:TypeToken<S>,
    receiverType:TypeToken<T>,
    rowID: RowID? = null,
    opts: CommonRowOptions? = null,
    initialCells: List<PrettyCellBase<*>> = emptyList(),
): PrettyRowBase<S, T>(sourceType, receiverType, initialCells,  rowID, opts), RenderableElement<S, T> {

    constructor(
        callCollection: CallableCollection<S, T>,
        rowID: RowID? = null,
        opts: CommonRowOptions? = null,
        initialCells: List<PrettyCellBase<*>> = emptyList(),
    ) : this(callCollection.parameterType, callCollection.resultType, rowID, opts, initialCells) {
        dataLoader.apply(callCollection)
    }

    override val name: String = "PrettyValueRow<${sourceType.typeName}, ${receiverType.typeName}>"
    override var dataLoader: DataLoader<S, T> = DataLoader("PrettyRow", sourceType, receiverType)

    override fun renderFromSource(source: S, opts: CommonRowOptions?): String {
        val rendered = mutableListOf<String>()
        val values = dataLoader.resolveList(source)
        values.forEach { value ->
            val render = doRender(value, opts)
            rendered.add(render)
        }
        return rendered.joinToString(SpecialChars.NEW_LINE)
    }

    fun RenderParameters.renderInScope(source: S): RenderData {
        val ownLoader = this@PrettyValueRow.dataLoader
        val receiver = ownLoader.resolveValue(source) {
            error("No source available", TraceOptions.ThisMethod)
        }
        return doScopedRender(this,  receiver)
    }

    override fun renderFromSource(marker: RenderMarker, source: S, opts: CommonRowOptions?): RenderData {
        val value = dataLoader.resolveValue(source) {
            error("renderFromSource", TraceOptions.ThisMethod)
        }
        return doRender(marker, value)
    }

    override fun copy(usingOptions: CommonRowOptions?): PrettyValueRow<S, T> {
        val copiedCells = cells.map { it.copy() }
        val rowCopy = if (usingOptions != null) {
            PrettyValueRow(sourceType, receiverType, templateID, usingOptions)
        } else {
            PrettyValueRow(sourceType, receiverType, templateID, options.copy())
        }
        rowCopy.dataLoader.apply(dataLoader.copy())
        rowCopy.initCells(copiedCells)
        return rowCopy
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PrettyValueRow<*, *>) return false
        if (other.templateID != templateID) return false
        if (other.sourceType != sourceType) return false
        if (other.receiverType != receiverType) return false
        if (other.cells != cells) return false
        if (other.dataLoader != dataLoader) return false
        return true
    }

    override fun hashCode(): Int {
        var result = (templateID.hashCode() ?: 0)
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + receiverType.hashCode()
        result = 31 * result + cells.hashCode()
        result = 31 * result + dataLoader.hashCode()
        return result
    }

    override fun toString(): String = infoString

    companion object : TemplateCompanion<PrettyValueRow<*, *>>, PrettyHelper {
        override val templateClass: KClass<PrettyValueRow<*, *>> = PrettyValueRow::class
        override val renderType: RenderableType = RenderableType.ValueGrid
        val sourceColour: Colour = Colour.GreenBright
        val prettyName: String = "ValueGrid".colorize(sourceColour)

        inline operator fun <reified S, reified T> invoke(
            property: KProperty1<S, T>,
            createParameter: RowBuildOption? = null,
            prettyCells: List<PrettyCellBase<*>> = emptyList(),
        ): PrettyValueRow<S, T> {
            val callable = property.toCallable()
            val row = when (createParameter) {

                is RowID -> PrettyValueRow(callable, rowID = createParameter, opts = null, prettyCells)
                is CommonRowOptions -> PrettyValueRow(callable, rowID = null, opts = createParameter, prettyCells)
                else -> PrettyValueRow(callable, rowID = null, opts = null, prettyCells)
            }
            return row
        }

        inline operator fun <reified S, reified T> invoke(
            property: KProperty1<S, T>,
            prettyCells: List<PrettyCellBase<*>> = emptyList(),
        ): PrettyValueRow<S, T> = PrettyValueRow(property.toCallable(), rowID = null, opts = null, prettyCells)

    }
}


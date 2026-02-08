package po.misc.data.pretty_print


import po.misc.callbacks.callable.CallableCollection
import po.misc.counters.SimpleJournal
import po.misc.data.ifUndefined
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.dsl.RenderConfigurator
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.options.CompositionTrace
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.TemplateData
import po.misc.data.pretty_print.parts.render.KeyParameters
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderParameters
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.interfaces.named.NamedComponent
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast
import po.misc.types.token.tokenOf
import kotlin.reflect.KClass


sealed class PrettyGridBase<T>(
    private val type: TypeToken<T>,
    opts: CommonRowOptions? = null,
): TokenFactory, PrettyHelper, NamedComponent, TokenHolder {

    var options: RowOptions = toRowOptions(opts)
    override var verbosity:Verbosity = Verbosity.Warnings

    abstract val templateData: TemplateData
    val templateID: GridID get() =  templateData.templateID as GridID

    internal var areOptionsExplicit: Boolean = false
    val journal: SimpleJournal = SimpleJournal(this::class)

    abstract val renderPlan: RenderPlan<*, T>
    abstract val enabled: Boolean
    val keyParameters: KeyParameters get() =  renderPlan.keyParams

    internal val prettyRows: MutableList<PrettyRow<T>> = mutableListOf()
    internal val valueRows : MutableList<PrettyValueRow<T, *>>  = mutableListOf()

    override val name: String get() = templateData.templateID.toString()

    protected val resolvedEmptyMsg: (valueType: TypeToken<*>, loaderMsg: String) -> String = { valueType, loaderMsg ->
        "Unable to resolve ${type.typeName} instance to any ${valueType.typeName}. DataLoader $loaderMsg"
    }
    val rows: List<PrettyRowBase<T, *>> get() = buildList {
        addAll(prettyRows)
        addAll(valueRows)
    }

    val size: Int get() = renderPlan.size

    fun useID(gridID: GridID) {
        val trace = CompositionTrace.createFrom(this, templateData.templateID.renderableType, gridID)
        templateData.updateID(trace)
    }

    fun <T2> addRowChecking(sourceType: TypeToken<T2>,rowBase: PrettyRowBase<T, *>):Boolean {
        when (rowBase) {
            is PrettyRow<*> -> {
                val checked = rowBase.safeCast<PrettyRow<T>, T>(type)
                if (checked != null) {
                    addRow(checked)
                    return true
                } else {
                    "Row $rowBase does not belong to this grid addRow rejected".output(Colour.Yellow)
                    return false
                }
            }
            is PrettyValueRow<*, *> -> {
                val checked = rowBase.safeCast<PrettyValueRow<T, T2>, T, T2>(type, sourceType)
                if (checked != null) {
                    addValueRow(checked)
                    return true
                } else {
                    "Row $rowBase does not belong to this grid addRow rejected".output(Colour.Yellow)
                    return false
                }
            }
        }
    }
    fun addRowChecking(rowBase: PrettyRowBase<T, *>):Boolean {
        when (rowBase) {
            is PrettyRow<*> -> {
                val checked = rowBase.safeCast<PrettyRow<T>, T>(type)
                if (checked != null) {
                    addRow(checked)
                    return true
                } else {
                    "Row $rowBase does not belong to this grid addRow rejected".output(Colour.Yellow)
                    return false
                }
            }

            is PrettyValueRow<*, *> -> {
                val checked = rowBase.safeCast<PrettyValueRow<T, *>, T>(type)
                if (checked != null) {
                    addValueRow(checked)
                    return true
                } else {
                    "Row $rowBase does not belong to this grid addRow rejected".output(Colour.Yellow)
                    return false
                }
            }
        }
    }

    fun addRow(row: PrettyRow<T>): PrettyRow<T>{
        prettyRows.add(row)
        renderPlan.add(row)
        return row
    }

    fun  addValueRow(valueRow: PrettyValueRow<T, *>): PrettyValueRow<T, *>{
        renderPlan.add(valueRow)
        valueRows.add(valueRow)
        return valueRow
    }
    fun  addValueRows(valueRows: List<PrettyValueRow<T, *>>){
        valueRows.forEach {
            addValueRow(it)
        }
    }
    open fun addRows(rows: List<PrettyRow<T>>): PrettyGridBase<T> {
        rows.forEach { addRow(it) }
        return this
    }

    fun render(receiver:T, opts: CommonRowOptions? = null):String {
        val useOptions = toRowOptions(opts, options)
        val optsChanged = options !== useOptions
        if(!renderPlan.initialized || optsChanged){
            renderPlan.initializeOptions(useOptions)
        }
        return renderPlan.render(receiver).styled
    }


    fun render(renderData: RenderData.Companion,  receiver:T): RenderCanvas{
        return renderPlan.render(receiver)
    }

    fun render(receiverList: List<T>, opts: CommonRowOptions? = null):String{
        val rendered = mutableListOf<String>()
        receiverList.forEach { receiver->
            val render =  render(receiver, opts)
            rendered.add(render)
        }
        return rendered.joinToString(SpecialChars.NEW_LINE)
    }

    fun applyOptions(opts: CommonRowOptions): PrettyGridBase<T>{
        val rowOptions =  toRowOptions(opts)
        options = rowOptions
        renderPlan.initializeOptions(options)
        areOptionsExplicit = true
        return this
    }
}

class PrettyGrid<T>(
    override val receiverType: TypeToken<T>,
    gridID: GridID? = null,
    opts: CommonRowOptions? = null,
): PrettyGridBase<T>(receiverType, opts), TemplateHost<T, T>
{
    override val sourceType: TypeToken<T> = receiverType
    override val renderableType: RenderableType =  RenderableType.Grid
    override val templateData: TemplateData = createGridData(CompositionTrace.createFrom(this, renderableType, gridID))
    override val dataLoader: DataLoader<T, T> = DataLoader("PrettyGrid", receiverType, receiverType)
    override var renderPlan: RenderPlan<T, T> = RenderPlan(this)
        private set

    override val enabled: Boolean  = true

    private fun preRenderConfig(configurator: (RenderConfigurator.()-> Unit)?){
        if(configurator != null) {
            val conf = RenderConfigurator()
            configurator.invoke(conf)
            conf.resolveAll(this)
        }
    }

    fun render(receiver:T, configurator: RenderConfigurator.()-> Unit):String{
        preRenderConfig(configurator)
       return render(receiver, options)
    }
    override fun renderFromSource(source: T, opts: CommonRowOptions?): String = render(source, opts)

    override fun copy(usingOptions: CommonRowOptions?): PrettyGrid<T>{
        val gridCopy =  PrettyGrid(receiverType,  templateID,  options.copy())
        gridCopy.dataLoader.apply(dataLoader.copy())
        gridCopy.renderPlan = renderPlan.copy()
        val copiedRows =  prettyRows.map { it.copy() }
        gridCopy.addRows(copiedRows)
        val copiedValueRows =  valueRows.map { it.copy() }
        gridCopy.addValueRows(copiedValueRows)
        return gridCopy
    }
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if(other !is PrettyGrid<*>) return false
        if(other.typeToken != typeToken) return false
        if(other.receiverType != receiverType) return false
        if(other.templateID != templateID) return false
        if(other.prettyRows != prettyRows) return false
        if(other.valueRows != valueRows) return false
        return true
    }
    override fun hashCode(): Int {
        var result = typeToken.hashCode()
        result = 31 * result + receiverType.hashCode()
        result = 31 * result + templateID.hashCode()
        result = 31 * result + prettyRows.hashCode()
        result = 31 * result + valueRows.hashCode()
        return result
    }

    companion object : TemplateCompanion<PrettyGrid<*>> {
        override val templateClass: KClass<PrettyGrid<*>> = PrettyGrid::class
        override val renderType : RenderableType = RenderableType.Grid
        val sourceColour: Colour = Colour.Blue
        val prettyName : String= "Grid".colorize(sourceColour)

        inline operator fun <reified T> invoke(
            gridID: GridID? = null,
            opts: CommonRowOptions? = null
        ): PrettyGrid<T> = PrettyGrid(tokenOf<T>(), gridID, opts)
    }
}

class PrettyValueGrid<S, T>(
    override val sourceType: TypeToken<S>,
    override val receiverType:TypeToken<T>,
    gridID: GridID? = null,
    opts: CommonRowOptions? = null,
) : PrettyGridBase<T>(receiverType, opts), TemplateHost<S, T>
{
    constructor(
        callableCollection: CallableCollection<S, T>,
        gridID: GridID? = null,
        opts: CommonRowOptions? = null
    ):this(callableCollection.parameterType, callableCollection.resultType, gridID, opts){
        dataLoader.apply(callableCollection)
    }

    override val renderableType: RenderableType =  RenderableType.ValueGrid
    override val templateData: TemplateData = createGridData(CompositionTrace.createFrom(this, renderableType, gridID))

    override val dataLoader : DataLoader<S, T> = DataLoader("PrettyValueGrid", sourceType, receiverType)
    override var renderPlan: RenderPlan<S, T>  = RenderPlan(this)
         private set

    override val enabled: Boolean get() = dataLoader.canResolve

    override fun renderFromSource(source: S, opts: CommonRowOptions?): String{
        val value = dataLoader.resolveList(source)
        ifUndefined(value){
            resolvedEmptyMsg(sourceType, "CanResolve: ${dataLoader.canResolve}").output(Colour.Yellow)
        }
        return render(value, opts)
    }

     fun RenderParameters.renderInScope(source: S): RenderCanvas{
        val ownLoader  = this@PrettyValueGrid.dataLoader
         ClassResolver.instanceMeta(this@renderInScope).output(Colour.CyanBright)
         val values = ownLoader.resolveList(source)
         val renders  = renderPlan.scopedRender(this, values)
         return renders
    }

    override fun addRows(rows: List<PrettyRow<T>>): PrettyValueGrid<S, T>{
        rows.forEach { addRow(it) }
        return this
    }
    override fun copy(usingOptions: CommonRowOptions? ): PrettyValueGrid<S, T>{
        val gridCopy =  PrettyValueGrid(sourceType, receiverType,  templateID,  options.copy())
        gridCopy.dataLoader.apply(dataLoader.copy())
        gridCopy.renderPlan =  renderPlan.copy()
        val copiedRows =  prettyRows.map { it.copy() }
        gridCopy.addRows(copiedRows)
        val copiedValueRows =  valueRows.map { it.copy() }
        gridCopy.addValueRows(copiedValueRows)
        return gridCopy
    }
    override fun equals(other: Any?): Boolean {
        if(other !is PrettyValueGrid<*,*>) return false
        if(other.receiverType != receiverType) return false
        if(other.sourceType != sourceType) return false
        if(other.dataLoader != dataLoader) return false
        if(other.templateID != templateID) return false
        if(other.prettyRows != prettyRows) return false
        if(other.valueRows != valueRows) return false
        return true
    }
    override fun hashCode(): Int {
        var result = receiverType.hashCode()
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + templateID.hashCode()
        result = 31 * result + dataLoader.hashCode()
        result = 31 * result + prettyRows.hashCode()
        result = 31 * result + valueRows.hashCode()
        return result
    }
    override fun toString(): String {
       return buildString {
           append("$templateID ")
           append("Enabled: $enabled ")
           append("Rows: ${rows.size} ")
        }
    }

    companion object: TemplateCompanion<PrettyValueGrid<*, *>> {
        override val templateClass: KClass<PrettyValueGrid<*, *>> = PrettyValueGrid::class
        override val renderType: RenderableType = RenderableType.ValueGrid
        val sourceColour: Colour = Colour.Green
        val prettyName: String = "ValueGrid".colorize(sourceColour)

        inline operator fun <reified S, reified T> invoke(
            callableCollection: CallableCollection<S, T>,
            gridID: GridID? = null,
            opts: CommonRowOptions? = null
        ): PrettyValueGrid<S, T>{
           return PrettyValueGrid(callableCollection, gridID, opts)
        }
    }
}


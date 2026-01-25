package po.misc.data.pretty_print.rows

import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.callbacks.validator.ValidityCondition
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.GridBuilderBase
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.parts.template.RenderController
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.parts.template.RowDelegate
import po.misc.data.pretty_print.parts.template.TemplateDelegate
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


sealed class RowBuilderBase<T>(
    val receiverType:TypeToken<T>,
): TokenFactory, TraceableContext, PrettyHelper{

    abstract val prettyRow: PrettyRowBase<*, T>
    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<*>>()
    internal val cells : List<PrettyCellBase<*>> get() = prettyCellsBacking
    internal val renderConditions = mutableListOf<ValidityCondition<T>>()

    protected val beforeRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()
    var options: RowOptions = RowOptions(Orientation.Horizontal)
        protected set

    var orientation : Orientation
        get() = options.orientation
        set(value) {
            options.orientation = value
        }

    @PublishedApi
    internal fun <C: PrettyCellBase<*>> storeCell(cell : C): C {
        prettyCellsBacking.add(cell)
        return cell
    }

    fun applyOptions(opts: CommonRowOptions?){
        options = toRowOptions(opts, options)
    }

    open fun finalizeRow(container: GridBuilderBase<*>? = null): PrettyRowBase<*, T> {
        prettyRow.initCells(cells)
        prettyRow.renderConditions.addAll(renderConditions.toList())
        prettyRow.options = options
        return prettyRow
    }

    fun acceptDelegate(delegate: RowDelegate<T>){
        delegate.attachHost(prettyRow)
    }

    fun withControl(controller: RenderController){
        controller.bind(prettyRow)
    }

    fun add(property: KProperty1<T, *>, opt: CellOptions? = null): KeyedCell<T>{
        val provider = property.toElementProvider(receiverType)

        val cell = KeyedCell(provider, toOptionsOrNull(opt))
        return storeCell(cell)
    }

    fun add(opts: CellOptions? = null, function: Function1<T, Any?>):KeyedCell<T> {
        val provider = function.toElementProvider(receiverType, TypeToken<Any?>())
        val cell = KeyedCell(provider).applyOptions(opts)
        return storeCell(cell)
    }
    fun add(content: String, opts: CellOptions? = null):StaticCell {
        val cell = StaticCell(content, opts)
        return storeCell(cell)
    }

    fun addAll(
        firstProperty: KProperty1<T, Any>,
        vararg property: KProperty1<T, Any>,
        opts: CellOptions? = null
    ): List<KeyedCell<T>> {
        val options = PrettyHelper.toOptions(opts, CellPresets.Property.asOptions())
        val cells = buildList {
            add( KeyedCell(firstProperty.toElementProvider(receiverType), options) )
            addAll(property.map { KeyedCell(it.toElementProvider(receiverType), options) })
        }
        prettyCellsBacking.addAll(cells)
        return cells
    }
    fun add(
        function: Function1<T, Any?>,
        opts: CellOptions? = null
    ): KeyedCell<T> {
        val provider = function.toElementProvider(receiverType, TypeToken<Any?>())
        val cellOptions = PrettyHelper.toOptions(opts)
        val keyedCell = KeyedCell(provider, toOptionsOrNull(opts))
        keyedCell.applyOptions(cellOptions)
        return storeCell(keyedCell)
    }

    inline fun <reified V : Any> add(
        property: KProperty1<T, V>,
        noinline builder: ComputedCell<T, V>.(V) -> Any,
    ): ComputedCell<T, V> {
        val provider = property.toElementProvider(receiverType,  tokenOf<V>())
        val computedCell = ComputedCell(provider, null, builder)
        return storeCell(computedCell)
    }

    fun build(opts: CellOptions? = null, builderAction: StringBuilder.()-> Unit): StaticCell {
        val cell = StaticCell(opts, builderAction)
        return storeCell(cell)
    }
}

class RowBuilder<T>(
    receiverType: TypeToken<T>,
    rowID: RowID? = null,
    opts: CommonRowOptions? = null,
    prettyRow: PrettyRow<T>? = null,
): RowBuilderBase<T>(receiverType), PrettyHelper {

    constructor(prettyRow: PrettyRow<T>):this(prettyRow.receiverType, prettyRow.templateID, prettyRow.options, prettyRow)
    override val prettyRow: PrettyRow<T> = prettyRow?: PrettyRow(receiverType, rowID, opts, emptyList())

    override fun finalizeRow(container: GridBuilderBase<*>?): PrettyRow<T> {
        super.finalizeRow(container)
        return prettyRow
    }

    fun addKeyless(prop: KProperty1<T, Any?>): KeyedCell<T> {
        val opts = Options(CellPresets.KeylessProperty)
        val provider =  prop.toElementProvider(receiverType)
        val cell = KeyedCell(provider, opts)
        return storeCell(cell)
    }
    fun addCell(opts: CellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opts)
        val cell = PrettyCell().applyOptions(options)
        return storeCell(cell)
    }
    fun addCells(vararg property: KProperty0<*>): List<KeyedCell<T>> {
        val cells = property.map { storeCell(KeyedCell(receiverType, it)) }
        return cells
    }
    override fun toString(): String {
        return buildString {
            append("RowBuilder of ${prettyRow.templateData}")
        }
    }

    companion object: TokenFactory {
        inline operator fun <reified T> invoke(rowID: RowID? = null, opts: CommonRowOptions? = null):RowBuilder<T>{
           return  RowBuilder(tokenOf<T>(), rowID, opts)
        }
    }
}

class ValueRowBuilder<S, T>(
    val sourceType: TypeToken<S>,
    receiverType: TypeToken<T>,
    rowID: RowID? = null,
    opts: CommonRowOptions? = null,
): RowBuilderBase<T>(receiverType){

    constructor(
        callable: CallableCollection<S, T>,
        rowID: RowID? = null
    ):this(callable.parameterType, callable.resultType, rowID){
        dataLoader.apply(callable)
    }

    override val prettyRow: PrettyValueRow<S, T> = PrettyValueRow(sourceType, receiverType, rowID,  opts)

    val dataLoader: DataLoader<S, T> get() =  prettyRow.dataLoader

    internal var renderKey: RenderKey? = null
    var preSavedBuilder: (ValueRowBuilder<S, T>.() -> Unit)? = null
        internal set
    var preSavedBuilderUsed: Boolean = false
        private set

    fun acceptDelegate(delegate: TemplateDelegate<T>){
        delegate.attachHost(prettyRow)
    }

    override fun finalizeRow(container: GridBuilderBase<*>?): PrettyValueRow<S, T>{
        preSavedBuilder?.invoke(this)
        super.finalizeRow(container)
        return prettyRow
    }

    fun preSaveBuilder(builder: ValueRowBuilder<S, T>.() -> Unit){
        preSavedBuilderUsed = true
        preSavedBuilder = builder
    }
    fun renderSourceHere(){
        renderKey = RenderKey(prettyRow.size, RenderableType.Row)
    }
    fun renderIf(predicate: (T) -> Boolean): ValueRowBuilder<S, T> {
        renderConditions.add(ValidityCondition("$this render if condition", predicate))
        return this
    }
    companion object
}





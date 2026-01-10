package po.misc.data.pretty_print.grid

import po.misc.callbacks.callable.asPropertyCallable
import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.counters.SimpleJournal
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.loader.toElementProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.data.pretty_print.templates.Lifecycle
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


sealed class GridBuilderBase<T>(
   val receiverType: TypeToken<T>
): TokenFactory, TraceableContext, PrettyHelper {

    internal var renderKey: RenderKey? = null
    internal val journal = SimpleJournal(ValueGridBuilder::class)
    abstract val prettyGrid: PrettyGridBase<T>
    val builderName: String = "GridBuilderBase<${receiverType.typeName}>"

    var options : RowOptions get() =  prettyGrid.options
        set(value) {
            prettyGrid.options = value
        }

    var orientation : Orientation
        get() = options.orientation
        set(value) {
           options.orientation = value
        }

    var verbosity: Verbosity
        get() =  prettyGrid.verbosity
        set(value) {
            prettyGrid.verbosity = value
        }

    protected var gridFinalized: Boolean = false

    open fun addRow(row: PrettyRow<T>): PrettyRow<T> = prettyGrid.addRow(row)
    open fun addRow(row: PrettyValueRow<T, *>): PrettyValueRow<T, *> = prettyGrid.addValueRow(row)

    open fun finalizeGrid(upperGrid: PrettyGridBase<*>): PrettyGridBase<T>{
        prettyGrid.verbosity = verbosity
        gridFinalized = true
        return prettyGrid
    }
    fun applyOptions(opt: CommonRowOptions): RowOptions {
        val created = toRowOptions(opt)
        prettyGrid.options = created
        return created
    }
    fun headedRow(text: String, id: RowID? = null, rowPreset: RowPresets = RowPresets.HorizontalHeaded): PrettyRow<T> {
        val options = rowPreset.asRowOptions().useId(id)
        val cell = StaticCell(text).applyOptions(options.cellOptions)
        val row =  PrettyRow(receiverType, id, options, cell.asList())
        addRow(row)
        return row
    }

    @PrettyDSL
    fun buildRow(rowID: RowID? = null, builder: RowBuilder<T>.() -> Unit):PrettyRow<T>{
        val container = RowBuilder(receiverType, rowID)
        builder.invoke(container)
        val row = container.finalizeRow(this)
        return addRow(row)
    }

    @PrettyDSL
    inline fun <reified  V> buildRow(
        property: KProperty1<T, V>,
        rowID: RowID? = null,
        builderAction: ValueRowBuilder<T, V>.() -> Unit
    ): PrettyValueRow<T, V>{
        val provider = property.asPropertyCallable(receiverType)
        val valueRowBuilder =  ValueRowBuilder(receiverType, provider.receiverType,  rowID)
        builderAction.invoke(valueRowBuilder)
        val valueRow =  valueRowBuilder.finalizeRow(this)
        prettyGrid.renderPlan.add(valueRow)
        return valueRow
    }

    @PrettyDSL
    fun useRow(
        row: PrettyRow<T>,
        builderAction: (RowBuilder<T>.() -> Unit)? = null
    ): PrettyRow<T>{
        val rowBuilder = RowBuilder(row.copy())
        builderAction?.invoke(rowBuilder)
        return rowBuilder.finalizeRow()
    }


    @PrettyDSL
    inline fun <reified V> useRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        parameter: Any? = null,
    ):PrettyValueGrid<T, V>{
        val valueGridBuilder = ValueGridBuilder(receiverType, row.receiverType)
        when{
            parameter != null && parameter is Orientation  -> { valueGridBuilder.prettyGrid.options.orientation = parameter }
            parameter != null && parameter is CommonRowOptions  -> { valueGridBuilder.applyOptions(parameter) }
            parameter != null && parameter is RowID  -> {}
        }
        val provider = property.toElementProvider(receiverType)
        valueGridBuilder.addRow(row.copy())
        return valueGridBuilder.finalizeGrid(provider, prettyGrid)
    }

    @JvmName("useRowList")
    @PrettyDSL
    inline fun <reified V> useRow(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        parameter: Any? = null,
    ):PrettyValueGrid<T, V>{

        val valueGridBuilder = ValueGridBuilder(receiverType, row.receiverType)
        when{
            parameter != null && parameter is Orientation  -> { valueGridBuilder.prettyGrid.options.orientation = parameter }
            parameter != null && parameter is CommonRowOptions  -> { valueGridBuilder.applyOptions(parameter) }
            parameter != null && parameter is RowID  -> {}
        }
        val provider = property.toElementProvider(receiverType)
        valueGridBuilder.addRow(row.copy())
        return valueGridBuilder.finalizeGrid(provider, prettyGrid)
    }


   fun useGrid(grid: PrettyGrid<T>) {
        grid.copy().rows.forEach { row ->
            prettyGrid.addRowChecking(row)
        }
    }


    @PublishedApi
    internal fun <T2> addRowsChecking(token: TypeToken<T2>,rows: List<PrettyRowBase<T, *>>){
        rows.forEach {
            prettyGrid.addRowChecking(token, it)
        }
    }

    inline fun <reified T2 : Any> createPlaceholder(lc: Lifecycle = Lifecycle.Reusable) {
        val template = TemplatePlaceholder(lc, prettyGrid, TypeToken<T2>())
        prettyGrid.renderPlan.add(template)
    }
}
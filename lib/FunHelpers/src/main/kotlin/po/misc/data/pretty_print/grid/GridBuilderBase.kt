package po.misc.data.pretty_print.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.counters.SimpleJournal
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.templates.PlaceholderLifecycle
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.functions.LambdaOptions
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


sealed class GridBuilderBase<T, V>(
    val hostType: TypeToken<T>,
    val type: TypeToken<V>,
): TokenFactory, TraceableContext,  PrettyHelper {

    abstract val prettyGrid: PrettyGridBase<T, V>
    internal var renderKey: RenderKey? = null
    var options: RowOptions? = null
        protected set

    internal val journal = SimpleJournal(ValueGridBuilder::class)
    val builderName: String = "GridBuilderBase<${hostType.typeName}, ${type.typeName}>"

    var orientation : Orientation
        get() = options?.orientation?:Orientation.Horizontal
        set(value) {
            options?.let { it.orientation = value }?:run {
                options = RowOptions(value)
            }
        }

    abstract val dataLoader: DataLoader<T, V>

    var verbosity: Verbosity
        get() =  prettyGrid.verbosity
        set(value) {
            prettyGrid.verbosity = value
        }

    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf()
    protected val beforeGridRender: Signal<GridParams, Unit> = signalOf()
    protected val templateResolved: Signal<PrettyValueGrid<T, *>, Unit> = signalOf()

    abstract  fun addRow(row: PrettyRow<V>): PrettyRow<V>

    fun applyOptions(opt: CommonRowOptions): RowOptions {
        val created =  toRowOptions(opt)
        options = created
        return created
    }
    fun headedRow(
        text: String,
        id: RowID? = null,
        rowPreset: RowPresets = RowPresets.HorizontalHeaded,
    ): PrettyRow<V> {
        val options = rowPreset.asRowOptions().useId(id)
        val cell = StaticCell(text).applyOptions(options.cellOptions)
        val row =  PrettyRow(type,  cell.asList(),  options, id)
        addRow(row)
        return row
    }
    fun onResolved(callback: V.(Unit) -> Unit): Unit {
        dataLoader.valueResolved.onSignal(LambdaOptions.Promise, callback = callback)
    }
    fun createRow(rowID: RowID, vararg cells: PrettyCellBase){
        val cells = cells.toList()
        val row = PrettyRow(cells, type, options = RowOptions(Orientation.Horizontal) , rowID)
        addRow(row)
    }
    fun setProviders(
        provider: (() -> V)? = null,
        listProvider: (() -> List<V>)? = null,
    ): GridBuilderBase<T, V> {
        if (provider != null) {
            dataLoader.setProvider(provider)
        }
        if (listProvider != null) {
            dataLoader.setListProvider(listProvider)
        }
        return this
    }

    @PublishedApi
    internal fun addRows(rows: List<PrettyRow<V>>) {
        rows.forEach { row ->
            addRow(row)
        }
    }
    fun useTemplate(row: PrettyRow<V>) {
        addRow(row.copy())
    }

    fun useTemplate(grid: PrettyGrid<V>) {
        val gridCopy = grid.copy()
        addRows(gridCopy.rows)
    }

    inline fun <reified T2 : Any> createPlaceholder(
        expectedClass: KClass<out T2>,
        lifecycle: PlaceholderLifecycle
    ) {
//       val template = TemplatePlaceholder(lifecycle,  this, TypeToken<T2>())
//       if(this is GridContainer<*>){
//           renderPlan.add(template)
//       }
    }
}
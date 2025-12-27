package po.misc.data.pretty_print.rows

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.callbacks.validator.ValidityCondition
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.GridBuilderBase
import po.misc.data.pretty_print.parts.cells.CellDelegate
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.toProvider
import po.misc.data.styles.Colour
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import java.util.function.Function
import kotlin.jvm.functions.FunctionN
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


sealed class RowBuilderBase<T, V>(
    val receiverType: TypeToken<T>,
    val valueType: TypeToken<V>,
): TokenFactory, TraceableContext, PrettyHelper{

    internal abstract val prettyRow: PrettyRow<V>
    internal val prettyCellsBacking = mutableListOf<PrettyCellBase>()
    internal val cells : List<PrettyCellBase> get() = prettyCellsBacking
    internal val renderConditions = mutableListOf<ValidityCondition<T>>()

    val dataLoader: DataLoader<T, V> = DataLoader("RowContainer", receiverType, valueType)
    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf<RowParams<V>, Unit>()
    var options: RowOptions = RowOptions(Orientation.Horizontal)
    protected set

    var orientation : Orientation
        get() = options.orientation
        set(value) {
            options.orientation = value
        }

    @PublishedApi
    internal fun <C: PrettyCellBase> storeCell(cell : C): C {
        prettyCellsBacking.add(cell)
        return cell
    }

    fun applyOptions(opt: CommonRowOptions): RowOptions {
        val created =  toRowOptions(opt)
        options = created
        return created
    }

    open fun finalizeRow(container: GridBuilderBase<*, *>? = null): PrettyRow<V> {
        for (cell in cells) {
            cell.setRow(prettyRow)
        }
        prettyRow.setCells(cells)
        prettyRow.renderConditions.addAll(renderConditions.toList())
        prettyRow.options = options
        return prettyRow
    }

    open fun add(property: KProperty1<V, *>, opt: CommonCellOptions? = null): KeyedCell<V>{
        val cell = KeyedCell( property.toProvider(valueType), toOptionsOrNull(opt))
        return storeCell(cell)
    }

    fun add(
        opts: CommonCellOptions? = null,
        function: Function1<V, Any?>
    ):KeyedCell<V> {
        val provider = function.toProvider(valueType, TypeToken<Any?>())
        val cell = KeyedCell(provider).applyOptions(opts)
        return storeCell(cell)
    }

    fun add(content: String, opt: CommonCellOptions? = null):StaticCell {
        val cellOptions = PrettyHelper.toOptions(opt)
        val cell = StaticCell(content).applyOptions(cellOptions)
        return storeCell(cell)
    }

    fun addAll(
        firstProperty: KProperty1<V, Any>,
        vararg property: KProperty1<V, Any>,
        cellOptions: CommonCellOptions? = null
    ): List<KeyedCell<V>> {
        val options = PrettyHelper.toOptions(cellOptions, CellPresets.Property.asOptions())
        val cells = buildList {
            add( KeyedCell(firstProperty.toProvider(valueType), options) )
            addAll(property.map { KeyedCell(it.toProvider(valueType), options) })
        }
        prettyCellsBacking.addAll(cells)
        return cells
    }
    fun add(
        function: Function1<V, Any?>,
        opts: CommonCellOptions? = null
    ): KeyedCell<V> {
        val provider = function.toProvider(valueType, TypeToken<Any?>())
        val cellOptions = PrettyHelper.toOptions(opts)
        val keyedCell = KeyedCell(provider, toOptionsOrNull(opts))
        keyedCell.applyOptions(cellOptions)
        return storeCell(keyedCell)
    }
    inline fun <reified V2 : Any> computed(
        property: KProperty1<V, V2>,
        opt: CommonCellOptions? = null,
        noinline builder: ComputedCell<V, V2>.(V2) -> Any,
    ): ComputedCell<V, V2> {
        val provider = property.toProvider(valueType,  tokenOf<V2>())
        val computedCell = ComputedCell(provider, opt, builder)
        return storeCell(computedCell)
    }

    fun build(opt: CommonCellOptions? = null, builderAction: StringBuilder.()-> Unit): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell().applyOptions(options).buildText(builderAction)
        return storeCell(cell)
    }
    fun beforeRowRender(callback: (RowParams<V>) -> Unit): Unit = beforeRowRender.onSignal(callback)
}




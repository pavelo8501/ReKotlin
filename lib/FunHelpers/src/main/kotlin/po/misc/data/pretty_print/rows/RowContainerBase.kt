package po.misc.data.pretty_print.rows

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.KeyedPresets
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


sealed class RowContainerBase<T: Any, V: Any>(
    val hostType: TypeToken<T>,
    val type: TypeToken<V>,
    val options: RowOptions
): TokenFactory{


    internal val prettyCellsBacking = mutableListOf<PrettyCellBase>()
    internal val cells : List<PrettyCellBase> get() = prettyCellsBacking

    val singleLoader: ValueLoader<T, V> = ValueLoader("RowContainer", hostType, type)
    val listLoader: ListValueLoader<T, V> = ListValueLoader("RowContainer", hostType, type)

    protected val beforeRowRender: Signal<RowParams<V>, Unit> = signalOf<RowParams<V>, Unit>()

    var rowId : Enum<*>?
        get() = options.rowId
        set(value) {
            options.rowId = value
        }

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
    @PublishedApi
    internal fun setProperty(property:KProperty1<T, V>) {
        singleLoader.setProperty(property)
    }
    @PublishedApi
    @JvmName("setPropertyList")
    internal fun setProperty(property:KProperty1<T, List<V>>) {
        listLoader.setProperty(property)
    }

    fun createRow(): PrettyRow<V> {
        val row = PrettyRow(type, options)
        cells.forEach {
            it.setRow(row)
        }
        row.setCells(cells)
        row.beforeRowRender.initializeBy(beforeRowRender)

        return row
    }

    fun addCell(
        content: String,
        opt: CommonCellOptions? = null
    ): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell(content).applyOptions(options)
        return storeCell(cell)
    }

    fun buildCell(opt: CommonCellOptions? = null, builderAction: StringBuilder.()-> Unit): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell().applyOptions(options).buildText(builderAction)
        return storeCell(cell)
    }

//    fun addCell(
//        property: KProperty1<V, Any>,
//        options: CommonCellOptions? = null
//    ): KeyedCell<V> {
//        val cellOptions = PrettyHelper.toKeyedOptionsOrNull(options)
//        val cell = KeyedCell(type, property).applyOptions(cellOptions)
//        return storeCell(cell)
//    }

    fun addCells(
        firstProperty: KProperty1<V, Any>,
        vararg property: KProperty1<V, Any>,
        cellOptions: CommonCellOptions? = null
    ): List<KeyedCell<V>> {
        val options = PrettyHelper.toKeyedOptions(cellOptions, KeyedPresets.Property.toOptions())
        val list = buildList {
            add(firstProperty)
            addAll(property.toList())
        }
        val cells = list.map { KeyedCell(type, it).applyOptions(options) }
        prettyCellsBacking.addAll(cells)
        return cells
    }

    fun beforeRowRender(callback: (RowParams<V>) -> Unit): Unit = beforeRowRender.onSignal(callback)

}




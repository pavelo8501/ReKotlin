package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.PrettyValueGrid
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
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1

sealed class RowContainerBase<T: Any, V: Any>(
    val typeToken: TypeToken<V>,
    val options: RowOptions
): TokenFactory{

    internal val prettyCellsBacking = mutableListOf<PrettyCellBase>()
    internal val cells : List<PrettyCellBase> get() = prettyCellsBacking
    abstract val prettyRow: PrettyRow<V>

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

    fun addCell(
        content: String,
        opt: CommonCellOptions? = null
    ): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell(content, prettyRow).applyOptions(options)
        return storeCell(cell)
    }

    fun buildCell(opt: CommonCellOptions? = null, builderAction: StringBuilder.()-> Unit): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell(prettyRow).applyOptions(options).buildText(builderAction)
        return storeCell(cell)
    }

    fun addCell(
        property: KProperty1<V, Any>,
        options: CommonCellOptions? = null
    ): KeyedCell<V> {
        val cellOptions = PrettyHelper.toKeyedOptionsOrNull(options)
        val cell = KeyedCell(typeToken, property, prettyRow).applyOptions(cellOptions)
        return storeCell(cell)
    }

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
        val cells = list.map { KeyedCell(typeToken, it, prettyRow).applyOptions(options) }
        prettyCellsBacking.addAll(cells)
        return cells
    }

    fun beforeRowRender(callback: (RowParams<V>) -> Unit){
        prettyRow.beforeRowRender(callback)
    }

}

class RowContainer<T: Any>(
    typeToken: TypeToken<T>,
    options: RowOptions
): RowContainerBase<T, T>(typeToken, options) {

    override val prettyRow: PrettyRow<T> = PrettyRow(typeToken, options, cells)

    val singleLoader: ValueLoader<T, T> = ValueLoader<T, T>("RowContainer", typeToken, typeToken)
    val listLoader: ListValueLoader<T, T> = ListValueLoader<T, T>("RowContainer", typeToken, typeToken)

    @PublishedApi
    internal fun applyBuilder(builder: RowContainer<T>.() -> Unit): PrettyRow<T> {
        builder.invoke(this)
        prettyRow.setCells(cells)
        return prettyRow
    }

    @PublishedApi
    internal fun applyParametrizedBuilder(
        parameter: T,
        builder: RowContainer<T>.(T) -> Unit
    ): PrettyRow<T> {
        builder.invoke(this, parameter)
        prettyRow.setCells(cells)
        return prettyRow
    }

    inline fun <reified V : Any> addCell(
        property: KProperty1<T, V>,
        opt: CommonCellOptions? = null,
        noinline action: ComputedCell<T, V>.(V) -> Any,
    ): ComputedCell<T, V> {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val valueToken = tokenOf<V>()
        val computedCell = ComputedCell(typeToken, valueToken, prettyRow, property, action)
        computedCell.applyOptions(options)
        return storeCell(computedCell)
    }

    fun addCell(opt: CommonCellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = PrettyCell(prettyRow).applyOptions(options)
        return storeCell(cell)
    }

    companion object
}

class RowValueContainer<T: Any, V: Any>(
    val hostTypeToken: TypeToken<T>,
    typeToken: TypeToken<V>,
    options: RowOptions
): RowContainerBase<T, V>(typeToken, PrettyHelper.toRowOptions(options)){

    override val prettyRow: PrettyRow<V> = PrettyRow<V>(typeToken,  options, cells)

    fun buildRow(
        builder: RowValueContainer<T, V>.()-> Unit
    ): PrettyRow<V>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        return prettyRow
    }

    fun buildGrid(
        property: KProperty1<T, V>,
        builder: RowValueContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        val grid = PrettyValueGrid(hostTypeToken, typeToken)
        grid.addRow(prettyRow)
        grid.singleLoader.setReadOnlyProperty(property)
        return grid
    }

    @JvmName("buildGridList")
    fun buildGrid(
        property: KProperty1<T, List<V>>,
        builder: RowValueContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        val grid = PrettyValueGrid(hostTypeToken, typeToken)
        grid.addRow(prettyRow)
        grid.listLoader.setReadOnlyProperty(property)
        return grid
    }

    companion object
}






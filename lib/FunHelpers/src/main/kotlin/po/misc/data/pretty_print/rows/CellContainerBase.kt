package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1

sealed class CellContainerBase<T: Any>(
    val typeToken:  TypeToken<T>,
    val options: RowOptions
): TokenFactory{


    internal val prettyCellsBacking = mutableListOf<PrettyCellBase>()
    val cells : List<PrettyCellBase> get() = prettyCellsBacking

    abstract val prettyRow: PrettyRow<T>

    @PublishedApi
    internal fun <C: PrettyCellBase> storeCell(cell : C): C {
        prettyCellsBacking.add(cell)
        return cell
    }

    fun buildCell(opt: CommonCellOptions? = null, builderAction: StringBuilder.()-> Unit): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = StaticCell(prettyRow).applyOptions(options).buildText(builderAction)
        return storeCell(cell)
    }
}

class CellContainer<T: Any>(
    typeToken: TypeToken<T>,
    options: RowOptions
): CellContainerBase<T>(typeToken, options) {

    override val prettyRow: PrettyRow<T> = PrettyRow(typeToken, cells, options)

    fun buildRow(builder: CellContainer<T>.() -> Unit): PrettyRow<T> {
        builder.invoke(this)
        prettyRow.setCells(cells)
        return prettyRow
    }

    fun addCell(
        content: String,
        opt: CommonCellOptions? = null
    ): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(opt)

        val cell = StaticCell(content, prettyRow).applyOptions(options)
        return storeCell(cell)
    }

    fun addCell(
        property: KProperty1<T, Any>,
        opt: CommonCellOptions? = null,
    ): KeyedCell<T> {
        val options = PrettyHelper.toKeyedOptionsOrNull(opt)
        val cell = KeyedCell(typeToken, property, prettyRow).applyOptions(options)
        return storeCell(cell)
    }

    inline fun <reified V : Any> addCell(
        property: KProperty1<T, V>,
        opt: CommonCellOptions? = null,
        noinline action: ComputedCell<T, V>.(V) -> Any,
    ): ComputedCell<T, V> {
        val cellOptions = PrettyHelper.toOptionsOrNull(opt)
        val valueToken = tokenOf<V>()
        val computedCell = ComputedCell(typeToken, valueToken, prettyRow, property, action)
        return storeCell(computedCell)
    }

    fun addCell(opt: CommonCellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = PrettyCell(prettyRow).applyOptions(options)
        return storeCell(cell)
    }

    fun addCells(
        firstProperty: KProperty1<T, Any>,
        vararg property: KProperty1<T, Any>,
        cellOptions: CommonCellOptions? = null
    ): List<KeyedCell<T>> {
        val options = PrettyHelper.toKeyedOptions(cellOptions)
        val list = buildList {
            add(firstProperty)
            addAll(property.toList())
        }
        val cells = list.map { KeyedCell(typeToken, it, prettyRow).applyOptions(options) }
        prettyCellsBacking.addAll(cells)
        return cells
    }




    companion object
}

class CellReceiverContainer<T: Any, V: Any>(
    val hostTypeToken: TypeToken<T>,
    val  valueToken: TypeToken<V>,
    options: RowOptions
): CellContainerBase<V>(valueToken, PrettyHelper.toRowOptions(options)){

    override val prettyRow: PrettyRow<V> = PrettyRow<V>(typeToken,  cells, options)

    fun buildRow(
        builder: CellReceiverContainer<T, V>.()-> Unit
    ): PrettyRow<V>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        return prettyRow
    }

    fun buildGrid(
        property: KProperty1<T, V>,
        builder: CellReceiverContainer<T, V>.()-> Unit
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
        builder: CellReceiverContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        val grid = PrettyValueGrid(hostTypeToken, typeToken)
        grid.addRow(prettyRow)
        grid.listLoader.setReadOnlyProperty(property)
        return grid
    }

    fun addCell(
        property: KProperty1<V, Any>,
        options: CommonCellOptions? = null
    ): KeyedCell<V> {
        val cellOptions = PrettyHelper.toKeyedOptionsOrNull(options)
        val cell = KeyedCell(typeToken, property, prettyRow).applyOptions(cellOptions)
        return storeCell(cell)
    }

    companion object
}






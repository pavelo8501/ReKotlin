package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.reflection.Readonly
import po.misc.reflection.resolveProperty
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

sealed class CellContainerBase<T: Any>(
    val typeToken:  TypeToken<T>,
    val options: RowOptions
){
    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<*>>()
    val cells : List<PrettyCellBase<*>> get() = prettyCellsBacking

    internal fun <C: PrettyCellBase<*>> storeCell(cell : C): C {
        prettyCellsBacking.add(cell)
        return cell
    }
}

class CellContainer<T: Any>(
    typeToken: TypeToken<T>,
    options: RowOptions
): CellContainerBase<T>(typeToken, options){

    internal val prettyRow = PrettyRow<T>(typeToken,  cells, options)

    fun buildRow(builder: CellContainer<T>.()-> Unit): PrettyRow<T>{
        builder.invoke(this)
        prettyRow.setCells(cells)
        return prettyRow
    }

    fun addCell(
        content: String,
        cellOptions: CommonCellOptions? = null
    ): StaticCell{
        val options = PrettyHelper.toCellOptionsOrDefault(cellOptions,  options)
        val cell = StaticCell(content, options, prettyRow)
        return  storeCell(cell)
    }

    fun addCell(
        property: KProperty1<T, Any>,
        options: CommonCellOptions? = null,
    ): KeyedCell<T> {
        val cellOptions =  PrettyHelper.toKeyedCellOptionsOrDefault(options)
        val cell = KeyedCell(typeToken, property, cellOptions, prettyRow)
        return storeCell(cell)
    }

    fun <V: Any> addCell(
        property: KProperty1<T, V>,
        cellOptions: CommonCellOptions? = null,
        action: ComputedCell<T, V>.(V)-> Any,
    ): ComputedCell<T, V> {
        val cellOptions =  PrettyHelper.toCellOptionsOrDefault(cellOptions, options)
        val computedCell = ComputedCell(typeToken, property, cellOptions, prettyRow, action)
        return storeCell(computedCell)
    }

    fun addCell(
        cellOptions: CommonCellOptions? = null
    ): PrettyCell{
        val options = PrettyHelper.toCellOptionsOrDefault(cellOptions,  options)
        val cell = PrettyCell(options, prettyRow)
        return  storeCell(cell)
    }

    fun addCells(
        firstProperty: KProperty1<T, Any>,
        vararg property : KProperty1<T, Any>,
        cellOptions: CommonCellOptions? = null
    ): List<KeyedCell<T>>{
        val options = PrettyHelper.toKeyedCellOptionsOrDefault(cellOptions)
        val list = buildList {
            add(firstProperty)
            addAll(property.toList())
        }
        val cells = list.map { KeyedCell(typeToken, it, options) }
        prettyCellsBacking.addAll(cells)
        return  cells
    }


    companion object
}

class CellReceiverContainer<T: Any, V: Any>(
    val hostTypeToken: TypeToken<T>,
    val  valueToken: TypeToken<V>,
    options: RowOptions
): CellContainerBase<V>(valueToken, PrettyHelper.toRowOptionsOrDefault(options)){

    @PublishedApi
    internal val prettyRow: PrettyRow<V> = PrettyRow<V>(typeToken,  cells, options)

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
        val cellOptions =  PrettyHelper.toKeyedCellOptionsOrDefault(options)
        val cell = KeyedCell(typeToken, property, cellOptions)
        return storeCell(cell)
    }

    companion object
}






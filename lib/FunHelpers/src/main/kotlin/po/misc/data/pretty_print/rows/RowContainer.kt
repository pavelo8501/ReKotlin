package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.strings.appendGroup
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


class RowContainer<T: Any>(
    type: TypeToken<T>,
    options: RowOptions
): RowContainerBase<T, T>(type, type, options) {

    @PublishedApi
    internal fun applyBuilder(builder: RowContainer<T>.() -> Unit): PrettyRow<T> {
        builder.invoke(this)
        return  createRow()
    }

    @PublishedApi
    internal fun applyParametrizedBuilder(
        parameter: T,
        builder: RowContainer<T>.(T) -> Unit
    ): PrettyRow<T> {
        builder.invoke(this, parameter)
        return  createRow()
    }

    fun addKeyless(
        prop: KProperty1<T, Any>,
        commonOpt: CommonCellOptions? = null
    ): KeyedCell<T>{
        val cellOptions =  Options(CellPresets.KeylessProperty)
        cellOptions.applyChanges(options.cellOptions)
        cellOptions.applyChanges(PrettyHelper.toOptionsOrNull(commonOpt))
        val cell = KeyedCell(type, prop, cellOptions)
        return storeCell(cell)
    }

    fun addCell(opt: CommonCellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = PrettyCell().applyOptions(options)
        return storeCell(cell)
    }

    fun addCells(vararg property: KProperty0<*>):List<KeyedCell<T>> {
        val cells = property.map { storeCell( KeyedCell(type).setSource(it) ) }
        return cells
    }

    override fun toString(): String {
       return buildString {
            appendGroup("RowContainer[", "]", ::rowId)
        }
    }


    companion object
}




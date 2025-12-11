package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
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

    fun addCell(
        property: KProperty1<T, Any>,
        opt: CommonCellOptions? = null,
    ): KeyedCell<T>{
        val cellOptions = PrettyHelper.toKeyedOptionsOrNull(opt)
        val cell = KeyedCell(type, property).applyOptions(cellOptions)
        return storeCell(cell)
    }

    inline fun <reified V : Any> addCell(
        property: KProperty1<T, V>,
        opt: CommonCellOptions? = null,
        noinline action: ComputedCell<T, V>.(V) -> Any,
    ): ComputedCell<T, V> {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val valueToken = tokenOf<V>()
        val computedCell = ComputedCell(type, valueToken, property, action)

        computedCell.applyOptions(options)
        return storeCell(computedCell)
    }

    fun addCell(opt: CommonCellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = PrettyCell().applyOptions(options)
        return storeCell(cell)
    }

    companion object
}




package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class RowValueContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    options: RowOptions
): RowContainerBase<T, V>(hostType, type, PrettyHelper.toRowOptions(options)){


    @PublishedApi
    internal fun applyBuilder(buildr: RowValueContainer<T, V>.()-> Unit): PrettyRow<V> {
        buildr.invoke(this)
        return createRow()
    }

    fun addCell(
        property: KProperty1<V, Any>,
        opt: CommonCellOptions? = null,
    ): KeyedCell<V> {
        val cellOptions = PrettyHelper.toKeyedOptionsOrNull(opt)
        val cell = KeyedCell(type, property).applyOptions(cellOptions)
        return storeCell(cell)
    }

    companion object
}

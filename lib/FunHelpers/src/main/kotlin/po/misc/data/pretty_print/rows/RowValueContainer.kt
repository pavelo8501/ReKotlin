package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


class RowValueContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    options: RowOptions
): RowContainerBase<T, V>(hostType, type, PrettyHelper.toRowOptions(options)){

   // override val prettyRow: PrettyRow<V> = PrettyRow<V>(type,  options, cells)

    @PublishedApi
    internal fun applyBuilder(buildr: RowValueContainer<T, V>.()-> Unit): PrettyRow<V>{
        buildr.invoke(this)
        return createRow()
    }
    companion object
}

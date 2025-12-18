package po.misc.data.pretty_print.rows

import po.misc.callbacks.validator.ValidityCondition
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.GridContainerBase
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class RowValueContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    options: RowOptions
): RowContainerBase<T, V>(hostType, type, PrettyHelper.toRowOptions(options)){

    override val prettyRow: PrettyRow<V> = PrettyRow(type, options)


    @PublishedApi
    internal fun initByGridContainer(gridContainer: GridContainerBase<T, *>){

        gridContainer.singleLoader.hostResolved.onSignal(this){host->
            if(renderConditions.isNotEmpty()){
                val result = renderConditions.first().validate(host)
                prettyRow.enabled = result
            }
        }
    }


    fun renderIf(predicate: (T) -> Boolean): RowValueContainer<T, V> {
        renderConditions.add(ValidityCondition("$this render if condition", predicate))

        return this
    }


    companion object
}

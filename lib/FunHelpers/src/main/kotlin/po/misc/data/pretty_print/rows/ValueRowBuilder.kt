package po.misc.data.pretty_print.rows

import po.misc.callbacks.validator.ValidityCondition
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.grid.GridBuilderBase
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.types.safeCast


class ValueRowBuilder<T, V>(
    val dataProvider: DataProvider<T, V>,
    rowID: RowID? = null,
): RowBuilderBase<T, V>(dataProvider.typeToken, dataProvider.valueType){

    internal var renderKey: RenderKey? = null
    override var prettyRow: PrettyRow<V> = PrettyRow.createEmpty(valueType, options, rowID)
        private set

    var preSavedBuilder:  (ValueRowBuilder<T, V>.() -> Unit)? = null
        internal set

    var isPreSaved:Boolean = false
    private set

    init {
        dataLoader.applyCallables(dataProvider)
    }

    override fun finalizeRow(container: GridBuilderBase<*, *>?): PrettyRow<V>{
       container?.safeCast<GridBuilderBase<T, *>>()?.let {
            it.dataLoader.hostResolved.onSignal(this){host->
                if(renderConditions.isNotEmpty()){
                    val result = renderConditions.first().validate(host)
                    prettyRow.enabled = result
                }
            }
        }
        preSavedBuilder?.invoke(this)
        return prettyRow
    }

    fun preSaveBuilder(builder: ValueRowBuilder<T, V>.() -> Unit){
        isPreSaved = true
        preSavedBuilder =builder
    }

    fun renderSourceHere(){
        renderKey = RenderKey(prettyRow.size, RenderableType.Row)
    }
    fun renderIf(predicate: (T) -> Boolean): ValueRowBuilder<T, V> {
        renderConditions.add(ValidityCondition("$this render if condition", predicate))
        return this
    }
    companion object
}

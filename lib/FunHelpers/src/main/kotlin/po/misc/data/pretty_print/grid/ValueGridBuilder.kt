package po.misc.data.pretty_print.grid

import po.misc.callbacks.callable.CallableStorage
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.counters.SimpleJournal
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.types.token.TypeToken

class ValueGridBuilder<T, V>(
    val sourceType:TypeToken<T>,
    receiverType:TypeToken<V>,
    gridID: GridID? = null,
    opts: CommonRowOptions? = null,
): GridBuilderBase<V>(receiverType), PrettyHelper{

    override val prettyGrid: PrettyValueGrid<T, V> = PrettyValueGrid(sourceType, receiverType, gridID, opts)

    var preSavedBuilder: (ValueGridBuilder<T, V>.() -> Unit)? = null
    var isPreSaved: Boolean = false
        internal set

    internal val rowsBacking = mutableListOf<PrettyRow<V>>()
    val rows: List<PrettyRowBase<V, *>> get() = prettyGrid.rows

    fun renderSourceHere(){
        renderKey = RenderKey(rowsBacking.size, RenderableType.Grid)
    }
    fun preSaveBuilder(builder:  ValueGridBuilder<T, V>.() -> Unit){
        isPreSaved = true
        preSavedBuilder = builder
    }
    fun applyOptions(opt: CommonRowOptions?){
        toRowOptionsOrNull(opt)?.let {
            options = it
        }
    }
    private fun orderRows(): Boolean{
        val rec =  journal.method("orderRows", SimpleJournal.ParamRec("renderKey", renderKey))
        val key = renderKey
        if(key != null){
            val takeCount = (key.order).coerceAtLeast(0)
            rowsBacking.take(takeCount).forEachIndexed { index, row ->
                prettyGrid.addRow(row)
                rowsBacking.removeAt(index)
            }
            return true
        }else{
            return false
        }
    }
    private fun transferRows(): Int{
        var transferred = 0
        rowsBacking.forEach { row ->
             prettyGrid.addRow(row)
            transferred ++
        }
        return transferred
    }



    fun finalizeGrid(sourceGrid: PrettyGridBase<T>): PrettyValueGrid<T, V>{
        isPreSaved = false
        journal.method("finalizeGrid", SimpleJournal.ParamRec("isPreSaved", isPreSaved))
        val ordered = orderRows()
        preSavedBuilder?.invoke(this)
        val transferred = transferRows()
        sourceGrid.renderPlan.add(prettyGrid)
        return prettyGrid
    }

    fun finalizeGrid(callable: ReceiverCallable<T, V>, sourceContainer: PrettyGridBase<T>): PrettyValueGrid<T, V>{
        prettyGrid.dataLoader.add(callable)
        return finalizeGrid(sourceGrid = sourceContainer)
    }
    @JvmName("finalizeGridListCallable")
    fun finalizeGrid(listCallable: ReceiverCallable<T, List<V>>, sourceContainer: PrettyGridBase<T>): PrettyValueGrid<T, V>{
        prettyGrid.dataLoader.add(listCallable)
        return finalizeGrid(sourceGrid = sourceContainer)
    }
    @JvmName("finalizeGridCallableRepositoryList")
    fun finalizeGrid(listRepository: CallableStorage<T, List<V>>, sourceContainer: PrettyGridBase<T>): PrettyValueGrid<T, V>{
        prettyGrid.dataLoader.apply(listRepository)
        return finalizeGrid(sourceGrid = sourceContainer)
    }
    fun finalizeGrid(elementRepository: CallableStorage<T, V>, sourceContainer: PrettyGridBase<T>): PrettyValueGrid<T, V>{
        prettyGrid.dataLoader.apply(elementRepository)
        return finalizeGrid(sourceGrid = sourceContainer)
    }
    override fun addRow(row: PrettyRow<V>): PrettyRow<V>{
        if(isPreSaved){
            rowsBacking.add(row)
        }else{
            prettyGrid.addRow(row)
        }
        return row
    }

    companion object
}

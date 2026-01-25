package po.misc.data.pretty_print.grid

import po.misc.callbacks.callable.CallableCollection
import po.misc.callbacks.callable.CallableStorage
import po.misc.callbacks.callable.PropertyCallable
import po.misc.callbacks.callable.ReceiverCallable
import po.misc.counters.SimpleJournal
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.ValueGridBuilder
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.template.TemplateDelegate
import po.misc.types.token.TypeToken
import po.misc.types.token.asElementType


class ValueGridBuilder<S, T>(
    val sourceType: TypeToken<S>,
    receiverType: TypeToken<T>,
    gridID: GridID? = null,
    opts: CommonRowOptions? = null,
): GridBuilderBase<T>(receiverType), PrettyHelper{

    constructor(
        callableCollection: CallableCollection<S, T>,
        gridID: GridID? = null,
    ):this(callableCollection.parameterType, callableCollection.resultType, gridID){
        prettyGrid.dataLoader.apply(callableCollection)
    }

    override val prettyGrid: PrettyValueGrid<S, T> = PrettyValueGrid(sourceType, receiverType, gridID, opts)

    var preSavedBuilder: (ValueGridBuilder<S, T>.() -> Unit)? = null
    var isPreSaved: Boolean = false
        internal set

    internal val rowsBacking = mutableListOf<PrettyRow<T>>()
    val rows: List<PrettyRowBase<T, *>> get() = prettyGrid.rows
    fun renderSourceHere(){
        renderKey = RenderKey(rowsBacking.size, RenderableType.Grid)
    }
    fun preSaveBuilder(builder:  ValueGridBuilder<S, T>.() -> Unit){
        isPreSaved = true
        preSavedBuilder = builder
    }

    private fun orderRows(): Boolean{
        val key = renderKey
        if(key != null){
            val takeCount = (key.index).coerceAtLeast(0)
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

    fun acceptDelegate(delegate: TemplateDelegate<*>){
        delegate.attachHost(prettyGrid)
    }

    fun finalizeGrid(sourceGrid: PrettyGridBase<S>): PrettyValueGrid<S, T>{
        isPreSaved = false
        journal.method("finalizeGrid", SimpleJournal.ParamRec("isPreSaved", isPreSaved))
        orderRows()
        preSavedBuilder?.invoke(this)
        transferRows()
        prettyGrid.applyOptions(options)
        sourceGrid.renderPlan.add(prettyGrid)
        return prettyGrid
    }
//    @JvmName("finalizeGridListCallable")
//    fun finalizeGrid(listCallable: ReceiverCallable<S, List<T>>, sourceContainer: PrettyGridBase<S>): PrettyValueGrid<S, T>{
//        prettyGrid.dataLoader.add(listCallable)
//        return finalizeGrid(sourceGrid = sourceContainer)
//    }
//    @JvmName("finalizeGridCallableRepositoryList")
//    fun finalizeGrid(listRepository: CallableStorage<S, List<T>>, sourceContainer: PrettyGridBase<S>): PrettyValueGrid<S, T>{
//        prettyGrid.dataLoader.apply(listRepository)
//        return finalizeGrid(sourceGrid = sourceContainer)
//    }
//    fun finalizeGrid(elementRepository: CallableStorage<S, T>, sourceContainer: PrettyGridBase<S>): PrettyValueGrid<S, T>{
//        prettyGrid.dataLoader.apply(elementRepository)
//        return finalizeGrid(sourceGrid = sourceContainer)
//    }
    override fun addRow(row: PrettyRow<T>): PrettyRow<T>{
        if(isPreSaved){
            rowsBacking.add(row)
        }else{
            prettyGrid.addRow(row)
        }
        return row
    }

    companion object {

        operator fun <S, T> invoke(
            property: PropertyCallable<S, List<T>>,
            gridID: GridID? = null,
        ): ValueGridBuilder<S, T> {
            val grid = ValueGridBuilder(property.parameterType, property.resultType.asElementType(), gridID)
            grid.prettyGrid.dataLoader.apply(property)
            return grid
        }
    }

}

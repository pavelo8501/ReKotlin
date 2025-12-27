package po.misc.data.pretty_print.grid

import po.misc.counters.SimpleJournal
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.PrettyHelper.Companion.toRowOptionsOrNull
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.styles.Colour


class ValueGridBuilder<T, V>(
    val dataProvider: DataProvider<T, V>,
    gridID: GridID? = null,
): GridBuilderBase<T, V>(dataProvider.typeToken,  dataProvider.valueType){

    constructor(
        dataProvider: DataProvider<T,  V>,
        rows: List<PrettyRow<V>>,
        opt: CommonRowOptions? = null,
        gridID: GridID? = null,
    ):this(dataProvider, gridID){
        applyOptions(opt)
        addRows(rows)
    }

    var preSavedBuilder: (ValueGridBuilder<T, V>.() -> Unit)? = null
    var isPreSaved: Boolean = false
        internal set

    override var prettyGrid: PrettyValueGrid<T, V> = PrettyValueGrid(dataProvider, gridID = gridID)
    override val dataLoader: DataLoader<T, V> get() =  prettyGrid.dataLoader

    internal val rowsBacking = mutableListOf<PrettyRow<V>>()
    val rows: List<PrettyRow<V>> get() = prettyGrid.rows

    init {
        dataLoader.applyCallables(dataProvider)
    }

    @PrettyDSL
    fun buildRow(
        rowID: RowID? = null,
        builder: RowBuilder<V>.() -> Unit
    ):PrettyRow<V>{
        val container = RowBuilder(type, rowID)
        builder.invoke(container)
        val row =  container.finalizeRow(this)
        return  addRow(row)
    }

    fun renderSourceHere(){
        renderKey = RenderKey(rowsBacking.size, RenderableType.Grid)
    }
    fun preSaveBuilder(builder:  ValueGridBuilder<T, V>.() -> Unit){
        isPreSaved = true
        preSavedBuilder =builder
    }
    fun applyOptions(opt: CommonRowOptions?){
        PrettyHelper.toRowOptionsOrNull(opt)?.let {
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

    internal fun finalizeGrid(row: PrettyRow<V>):PrettyValueGrid<T, V>{
        if(renderKey != null){
            val ordered = orderRows()
            prettyGrid.addRow(row)
        }
        return prettyGrid
    }

    fun finalizeGrid(sourceContainer: GridBuilderBase<*, *>? = null): PrettyValueGrid<T, V>{
        isPreSaved = false
        journal.method("finalizeGrid", SimpleJournal.ParamRec("isPreSaved", isPreSaved))
        val ordered = orderRows()
        preSavedBuilder?.invoke(this)
        val transferred = transferRows()
        return prettyGrid
    }


    override fun addRow(row: PrettyRow<V>): PrettyRow<V>{
        if(isPreSaved){
            rowsBacking.add(row)
        }else{
            prettyGrid.addRow(row)
        }
        return row
    }

    companion object{

    }
}

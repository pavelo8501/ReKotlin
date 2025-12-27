package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.TransitionGrid
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.RowOptions


class TransitionContainer<T, V>(
    val dataProvider: DataProvider<T, V>,
    val sourceGrid: PrettyGrid<V>,
    options: RowOptions =  sourceGrid.options
): GridBuilderBase<T, V>(dataProvider.typeToken,  sourceGrid.receiverType){

    override val prettyGrid: TransitionGrid<T, V> = TransitionGrid(dataProvider, sourceGrid)
    val rows: List<PrettyRow<V>> = prettyGrid.rows
    override val dataLoader: DataLoader<T, V> = DataLoader("Transition", hostType, type)
    fun renderSourceHere(){
        renderKey = RenderKey(rows.size, RenderableType.Grid)
    }
    override fun addRow(row: PrettyRow<V>): PrettyRow<V>{
       return prettyGrid.addRow(row)
    }
    fun finalizeGrid(sourceContainer: HostGridBuilder<*>? = null): TransitionGrid<T, V>{
        return prettyGrid
    }
}

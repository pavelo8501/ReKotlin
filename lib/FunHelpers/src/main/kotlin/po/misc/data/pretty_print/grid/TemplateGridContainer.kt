package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowOptionsEditor
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.copyRow
import po.misc.types.token.TypeToken


class TemplateGridContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    var options: RowOptionsEditor = RowOptions()
): GridContainerBase<T, V>(hostType, type), RowOptionsEditor by options{

    constructor(grid: PrettyGridBase<T, V>):this(grid.hostType, grid.type){
        initializeByGrid(grid)
    }

    private var ownHostTypeInit:Boolean = true
    private val tempRows = mutableListOf<PrettyRow<V>>()

    internal var pluggedKey:  GridKey? = null

    private fun assembleRows(): List<PrettyRow<V>>{
        val result = mutableListOf<PrettyRow<V>>()
        val key = pluggedKey
        if (key != null) {
            result.addAll(rows)
            result.addAll(key.order, tempRows)
        }else{
            result.addAll(tempRows)
            result.addAll(rows)
        }
        return result
    }

    internal fun createGrid():PrettyGrid<V> {
        val useOptions =  options.takeIf { options.edited }
        useOptions?.noEdit()
        @Suppress("DuplicatedCode")
        val grid =  GridContainer.buildGridCopying(this, useOptions as RowOptions?){grid->
            val rowsToAdd = assembleRows()
            rowsToAdd.forEach {
                grid.addRow(it)
            }
        }
        return grid
    }

    internal fun createValueGrid():PrettyValueGrid<T, V> {
        val useOptions =  options.takeIf { options.edited }
        useOptions?.noEdit()
        @Suppress("DuplicatedCode")
        val valueGrid =   GridValueContainer.buildGridCopying(this, useOptions as RowOptions?){grid->
            val rowsToAdd = assembleRows()
            rowsToAdd.forEach {
                grid.addRow(it)
            }
        }
        return valueGrid
    }

    @PublishedApi
    internal fun buildValueGrid(builder: TemplateGridContainer<T, V>.()-> Unit): PrettyValueGrid<T, V> {
        builder.invoke(this)
        val grid = createValueGrid()
        return grid
    }

    @PublishedApi
    internal fun buildGrid(builder: TemplateGridContainer<T, V>.()-> Unit): PrettyGrid<V> {
        builder.invoke(this)
        val grid = createGrid()
        return grid
    }

    fun initializeByContainer(rowContainer: RowContainer<V>){
        ownHostTypeInit = rowContainer.hostType == hostType
        if(rowContainer.options.useNoEdit){
            options = rowContainer.options
        }
        singleLoader.initValueFrom(rowContainer.singleLoader)
        listLoader.initValueFrom(rowContainer.listLoader)
        tempRows.add(rowContainer.createRow())
    }

    fun initializeByGrid(grid: PrettyGridBase<*, V>){
        ownHostTypeInit = grid.hostType == hostType
        if(grid.options.useNoEdit){
            options = grid.options
        }
        singleLoader.initValueFrom(grid.singleLoader)
        listLoader.initValueFrom(grid.listLoader)
        val ownRows = grid.rows.map { it.copyRow(type) }
        tempRows.addAll(ownRows)
    }

    fun renderHere(){
        if(pluggedKey == null){
            val insertToIndex = rows.lastIndex + 1
            pluggedKey =  GridKey(insertToIndex, GridSource.Renderable)
        }
    }

    companion object

}
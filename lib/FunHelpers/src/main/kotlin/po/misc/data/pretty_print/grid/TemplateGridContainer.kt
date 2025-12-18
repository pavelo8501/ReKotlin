package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowOptionsEditor
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.data.pretty_print.rows.copyRow
import po.misc.data.pretty_print.rows.createRowContainer
import po.misc.types.token.TypeToken



class TemplateGridContainer<T: Any, V: Any>(
    override val hostType: TypeToken<T>,
    override val type: TypeToken<V>,
    var options: RowOptionsEditor = RowOptions()
): TemplateBuilderScope<T, V>, RowOptionsEditor by options{

    constructor(grid: PrettyGridBase<T, V>):this(grid.hostType, grid.type){
        initializeByGrid(grid)
    }


    val source: GridValueContainer<T, V> = GridValueContainer(hostType, type)

    private var ownHostTypeInit:Boolean = true
    private val tempRows = mutableListOf<PrettyRow<V>>()

    internal var pluggedKey:  GridKey? = null

    private fun assembleRows(): List<PrettyRow<V>>{
        val result = mutableListOf<PrettyRow<V>>()
        val key = pluggedKey
        if (key != null) {
            result.addAll(source.rows)
            result.addAll(key.order, tempRows)
        }else{
            result.addAll(tempRows)
            result.addAll(source.rows)
        }
        return result
    }

    internal fun createGrid(): PrettyGrid<V> {
        val useOptions =  options.takeIf { options.edited }
        useOptions?.noEdit()
        @Suppress("DuplicatedCode")
        val grid =  GridContainer.buildGridCopying<T, V>(source, useOptions as RowOptions?){grid->
            val rowsToAdd = assembleRows()
            rowsToAdd.forEach {
                grid.addRow(it)
            }
        }
        return grid
    }

    internal fun createValueGrid(): PrettyValueGrid<T, V> {
        val useOptions =  options.takeIf { options.edited }
        useOptions?.noEdit()
        @Suppress("DuplicatedCode")
        val valueGrid =   GridValueContainer.buildGridCopying(source, useOptions as RowOptions?){grid->
            val rowsToAdd = assembleRows()
            rowsToAdd.forEach {
                grid.addRow(it)
            }
        }
        return valueGrid
    }

    override fun addRow(row: PrettyRow<V>): GridKey? = source.addRow(row)

//    @PublishedApi
//    internal fun buildValueGrid(builder: TemplateGridContainer<T, V>.()-> Unit): PrettyValueGrid<T, V> {
//        builder.invoke(this)
//        val grid = createValueGrid()
//        return grid
//    }
//
//    @PublishedApi
//    internal fun buildGrid(builder: TemplateGridContainer<T, V>.()-> Unit): PrettyGrid<V> {
//        builder.invoke(this)
//        val grid = createGrid()
//        return grid
//    }
//
    override fun buildRow(
        rowOptions: CommonRowOptions?,
        builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options as RowOptions)
        options.noEdit()
        val container = RowValueContainer(hostType, type, options)
        val row =  container.applyBuilder(builder)
        addRow(row)
    }

    fun initializeByContainer(rowContainer: RowContainer<V>){
        ownHostTypeInit = rowContainer.hostType == hostType
        if(rowContainer.options.sealed){
            options = rowContainer.options
        }
        source.singleLoader.initValueFrom(rowContainer.singleLoader)
        source.listLoader.initValueFrom(rowContainer.listLoader)
        tempRows.add(rowContainer.createRow())
    }

    fun initializeByGrid(grid: PrettyGridBase<*, V>){
        ownHostTypeInit = grid.hostType == hostType
        if(grid.options.sealed){
            options = grid.options
        }
        source.singleLoader.initValueFrom(grid.singleLoader)
        source.listLoader.initValueFrom(grid.listLoader)
        val ownRows = grid.rows.map { it.copyRow(type) }
        tempRows.addAll(ownRows)
    }

    override fun renderHere(){
        if(pluggedKey == null){
            val insertToIndex = source.rows.lastIndex + 1
            pluggedKey =  GridKey(insertToIndex, GridSource.Renderable)
        }
    }

    companion object

}
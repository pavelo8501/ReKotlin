package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.grid.GridKey
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.data.pretty_print.rows.createRowValueContainer
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class GridValueContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    var options: RowOptions = RowOptions()
): GridContainerBase<T, V>(hostType, type){

    constructor(
        hostType: TypeToken<T>,
        type: TypeToken<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        options: RowOptions = RowOptions()
    ):this(hostType, type, options){

        if(property != null){
            setProperty(property)
        }
        if(listProperty != null){
            listLoader.setProperty(listProperty)
        }
    }

    constructor(rowContainer: RowValueContainer<T, V>):this(rowContainer.hostType, rowContainer.type){
        initializeByContainer(rowContainer)
    }

    constructor(grid: PrettyGridBase<T, V>):this(grid.hostType, grid.type){
        initializeByGrid(grid)
    }

    override val grid: PrettyValueGrid<T, V> = PrettyValueGrid(hostType, type, options)

    private val rowsBacking = mutableListOf<PrettyRow<V>>()
    override val rows : List<PrettyRow<V>> = rowsBacking

    @PublishedApi
    internal fun initGrid(opts: RowOptions? = null):PrettyValueGrid<T, V>{
        grid.addRows(rows)
        if(opts != null){
            grid.applyOptions(opts)
        }
        return grid
    }

    @PublishedApi
    internal fun addRowContainer(container: RowValueContainer<T, V>){
        singleLoader.initFrom(container.singleLoader)
        listLoader.initFrom(container.listLoader)
        addRow(container.initRow())
    }

    override fun addRow(row: PrettyRow<V>): GridKey?{
        rowsBacking.add(row)
        return null
    }

    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptionsOrNull(rowOptions)
        options?.noEdit()
        val container = createRowValueContainer(hostType, type, options)
        builder.invoke(container)
        container.initByGridContainer(this)
        val row = container.initRow()
        addRow(row)
    }

    fun initializeByGrid(grid: PrettyGridBase<T, V>){
        singleLoader.initFrom(grid.singleLoader)
        listLoader.initFrom(grid.listLoader)
        options = grid.options
        addRows(grid.rows)
    }

    fun initializeByContainer(container: RowValueContainer<T, V>){
        singleLoader.initFrom(container.singleLoader)
        listLoader.initFrom(container.listLoader)
        options = container.options
        addRow(container.initRow())
    }

    companion object{
        fun <T: Any, V: Any> buildGridCopying(
            container: GridContainerBase<T, V>,
            opts: RowOptions? = null,
            builder: (GridValueContainer<T, V>)-> Unit
        ): PrettyValueGrid<T, V> {
            val gridContainer = GridValueContainer(container.hostType, container.type)
            gridContainer.singleLoader.initValueFrom(container.singleLoader)
            gridContainer.listLoader.initValueFrom(container.listLoader)
            builder.invoke(gridContainer)
            return gridContainer.grid
        }
    }
}

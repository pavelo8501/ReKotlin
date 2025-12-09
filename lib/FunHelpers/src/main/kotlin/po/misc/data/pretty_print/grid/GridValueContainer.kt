package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class GridValueContainer<T: Any, V: Any>(
    hostTypeToken: TypeToken<T>,
    typeToken: TypeToken<V>,
    var options: RowOptions? = null
): GridContainerBase<T, V>(hostTypeToken, typeToken){

    constructor(hostType: TypeToken<T>, grid: PrettyGridBase<*, V>):this(hostType, grid.typeToken){
        initializeByGrid(grid)
    }
    constructor(hostType: TypeToken<T>, rowContainer: RowContainer<V>):this(hostType, rowContainer.typeToken){
        consumeRowContainer(rowContainer)
    }

    fun createValueGrid(opts: RowOptions? = null):PrettyValueGrid<T, V> {
        val useOptions = opts?:options
        val grid =  PrettyValueGrid(hostTypeToken, typeToken)
        grid.applyOptions(useOptions)
        grid.addRows(rows)
        grid.singleLoader.initValueFrom(singleLoader)
        grid.listLoader.initValueFrom(listLoader)
        return grid
    }

    fun createGrid(opts: RowOptions? = null):PrettyGrid<V> {
        val useOptions = opts?:options
        val grid =  PrettyGrid(typeToken)
        grid.applyOptions(useOptions)
        grid.addRows(rows)
        grid.singleLoader.initValueFrom(singleLoader)
        grid.listLoader.initValueFrom(listLoader)
        return grid
    }

    @PublishedApi
    internal fun applyBuilder(buildr: GridValueContainer<T, V>.()-> Unit):PrettyValueGrid<T, V>{
        buildr.invoke(this)
        val valueGrid = createValueGrid()
        return valueGrid
    }

    @PublishedApi
    internal fun applyBuilder(property: KProperty1<T, V>, builder: GridValueContainer<T, V>.()-> Unit):PrettyValueGrid<T,V>{
       val valueGrid = applyBuilder(builder)
        valueGrid.singleLoader.setReadOnlyProperty(property)
        return valueGrid
    }

    @PublishedApi
    @JvmName("buildGridList")
    internal fun applyBuilder(property: KProperty1<T, List<V>>, builder: GridValueContainer<T, V>.()-> Unit):PrettyValueGrid<T, V>{
        val valueGrid = applyBuilder(builder)
        valueGrid.listLoader.setReadOnlyProperty(property)
        return valueGrid
    }

    @PublishedApi
    internal fun applyBuilder(provider: ()-> V, builder: GridValueContainer<T, V>. ()-> Unit):PrettyValueGrid<T,V>{
        val valueGrid = applyBuilder(builder)
        valueGrid.singleLoader.setProvider(provider)
        return valueGrid
    }

    @PublishedApi
    internal fun addRowContainer(container: RowValueContainer<T, V>, property: KProperty1<T, V>): RowValueContainer<T, V>{
        val valueGrid = createValueGrid()
        valueGrid.singleLoader.setReadOnlyProperty(property)
        valueGrid.addRow(container.prettyRow)
        return container
    }
    fun initializeByGrid(property: KProperty1<T, V>, grid: PrettyGrid<V>): PrettyValueGrid<T, V>{
        val valueGrid = createValueGrid()
        valueGrid.singleLoader.setReadOnlyProperty(property)
        grid.rows.forEach {
            it.applyOptions(grid.options)
            valueGrid.addRow(it)
        }
        return valueGrid
    }

    fun initializeByGrid(grid: PrettyGridBase<*, V>){
        addRows(grid.rows)
        options = grid.options
    }

    fun consumeRowContainer(rowContainer: RowContainer<V>){
        addRow(rowContainer.prettyRow)
        options = rowContainer.options
        listLoader.initValueFrom(rowContainer.listLoader)
        singleLoader.initValueFrom(rowContainer.singleLoader)
    }

}


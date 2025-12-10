package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.properties.checkType
import po.misc.properties.isReturnTypeList
import po.misc.types.token.TypeToken
import po.misc.types.token.safeParametrizedCast
import kotlin.reflect.KProperty1


class GridValueContainer<T: Any, V: Any>(
    hostType: TypeToken<T>,
    type: TypeToken<V>,
    var options: RowOptions? = null
): GridContainerBase<T, V>(hostType, type){


    constructor(hostType: TypeToken<T>, type: TypeToken<V>, property: KProperty1<T, V>):this(hostType, type){
        if(type.isCollection){
            if(!property.isReturnTypeList){
                throw IllegalStateException("Something went wrong. Type token isCollection = true but property isReturnTypeList = false")
            }

            val checked = property.checkType(hostType, type)

            listLoader.setProperty(property)
        }else{
            singleLoader.setProperty(property)
        }
    }

    constructor(rowContainer: RowValueContainer<T, V>):this(rowContainer.hostType, rowContainer.type){
        initializeByContainer(rowContainer)
    }

    constructor(grid: PrettyGridBase<T, V>):this(grid.hostType, grid.type){
        initializeByGrid(grid)
    }




    @PublishedApi
    internal fun createValueGrid(opts: RowOptions? = null): PrettyValueGrid<T, V> {
        val valueGrid = PrettyValueGrid(hostType, type)
        valueGrid.rowsBacking = rowsBacking
        valueGrid.renderMapBacking = renderMapBacking
        valueGrid.singleLoader.initValueFrom(singleLoader)
        valueGrid.listLoader.initValueFrom(listLoader)
        valueGrid.applyOptions(opts)
        return valueGrid
    }


    @PublishedApi
    internal fun applyBuilder(
        buildr: GridValueContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        buildr.invoke(this)
        val valueGrid = createValueGrid()
        return valueGrid
    }

    @PublishedApi
    internal fun applyBuilder(provider: ()-> V, builder: GridValueContainer<T, V>. ()-> Unit): PrettyValueGrid<T, V> {
        val valueGrid = applyBuilder(builder)
        valueGrid.singleLoader.setProvider(provider)
        return valueGrid
    }

    @PublishedApi
    internal fun addRowContainer(container: RowValueContainer<T, V>){
        singleLoader.initFrom(container.singleLoader)
        listLoader.initFrom(container.listLoader)
        addRow(container.createRow())
    }


    fun setProperties(
        property:  KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null
    ){
        if(property != null){
            singleLoader.setProperty(property)
        }
        if(listProperty != null){
            listLoader.setProperty(listProperty)
        }
    }


    fun initializeByGrid(property: KProperty1<T, V>, grid: PrettyGrid<V>): PrettyValueGrid<T, V> {
        val valueGrid = createValueGrid()
        valueGrid.singleLoader.setProperty(property)
        grid.rows.forEach {
            it.applyOptions(grid.options)
            valueGrid.addRow(it)
        }
        return valueGrid
    }

    fun initializeByGrid(hostType: TypeToken<T>,  grid: PrettyGridBase<*, V>){
        val casted = grid.safeParametrizedCast<PrettyGridBase<T, V>>(hostType)
        if(casted != null){
            singleLoader.initFrom(casted.singleLoader)
            listLoader.initFrom(casted.listLoader)
            options = casted.options
            addRows(casted.rows)
            renderMapBacking = casted.renderMapBacking
        }
    }

    fun initializeByGrid(grid: PrettyGridBase<T, V>){
        singleLoader.initFrom(grid.singleLoader)
        listLoader.initFrom(grid.listLoader)
        options = grid.options

        addRows(grid.rows)
        renderMapBacking = grid.renderMapBacking
    }

    fun initializeByContainer(container: RowValueContainer<T, V>){
        singleLoader.initFrom(container.singleLoader)
        listLoader.initFrom(container.listLoader)
        options = container.options
        addRow(container.createRow())
    }

    fun consumeRowContainer(rowContainer: RowContainer<V>){
        addRow(rowContainer.createRow())
        options = rowContainer.options
        listLoader.initValueFrom(rowContainer.listLoader)
        singleLoader.initValueFrom(rowContainer.singleLoader)
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
            return gridContainer.createValueGrid(opts)
        }
    }

}
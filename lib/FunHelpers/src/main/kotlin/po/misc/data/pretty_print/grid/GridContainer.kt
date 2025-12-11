package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.rows.RowBuilderScope
import po.misc.data.pretty_print.rows.RowContainer
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.data.pretty_print.rows.copyRow
import po.misc.data.pretty_print.rows.createRowContainer
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1



class GridContainer<T: Any>(
    type: TypeToken<T>,
    var options: RowOptions? = null
): GridContainerBase<T, T>(type, type), RowBuilderScope<T>{

    internal fun createGrid(opts: RowOptions? = null): PrettyGrid<T> {
        val grid = PrettyGrid(type)
        grid.singleLoader.initValueFrom(singleLoader)
        grid.listLoader.initValueFrom(listLoader)
        grid.gridMap = gridMap
        grid.renderMapBacking = renderMapBacking
        grid.rowsBacking.addAll(rows)
        grid.applyOptions(opts)
        return grid
    }

    @PublishedApi
    internal fun applyBuilder(buildr: GridContainer<T>.()-> Unit): PrettyGrid<T> {
        buildr.invoke(this)
        val prettyGrid: PrettyGrid<T> = createGrid(options)
        return prettyGrid
    }

    override fun addRow(row: PrettyRow<T>): GridKey {
        rowsBacking.add(row)
        return addRenderBlock(row)
    }

    @PrettyDSL
    inline fun <reified V: Any> buildRow(
        property: KProperty1<T, V>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)

        val rowContainer = RowValueContainer(hostType, TypeToken.create<V>(), options)
        rowContainer.setProperty(property)
        val valueGrid =  rowContainer.applyBuilder(builder)
        val container = GridValueContainer(rowContainer)
        val grid = container.createValueGrid(null)
        addRenderBlock(grid)
    }

    @PrettyDSL
    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<T>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptionsOrNull(rowOptions)
        options?.noEdit()
        val container = createRowContainer(type, options)
        val row =  container.applyBuilder(builder)
        addRow(row)
    }

    @PrettyDSL
    inline fun <reified V: Any> buildListRow(
        property: KProperty1<T, List<V>>,
        rowOptions: CommonRowOptions? = null,
        noinline builder: RowValueContainer<T, V>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptions(rowOptions, options)
        val type = TypeToken.create<V>()
        val rowContainer = RowValueContainer(hostType,type, options)
        rowContainer.setProperty(property)
        rowContainer.applyBuilder(builder)
        val container =  GridValueContainer(rowContainer)
        val grid = container.createValueGrid(null)
        addRenderBlock(grid)
    }


    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGrid(type,  property, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val casted = grid.castOrThrow<PrettyValueGrid<T, V>>()
                casted.singleLoader.setProperty(property)
                addRenderBlock(casted)
                return casted
            }
        }
    }

    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        provider: ()-> V
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGrid(type,  provider, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.singleLoader.setProvider(provider)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    fun <V: Any> useListTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, List<V>>,
        orientation: Orientation = grid.options.orientation
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        optionCopy.orientation = orientation
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGridList(type, property, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setProperty(property)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    fun <V: Any> useListTemplate(
        grid: PrettyGridBase<*, V>,
        orientation: Orientation = grid.options.orientation,
        provider: () -> List<V>
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        optionCopy.orientation = orientation
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGridList(type, provider, optionCopy)
                addRenderBlock(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid -> {
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setProvider(provider)
                addRenderBlock(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    private  fun <V: Any> useRowTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        opts: RowOptions? = null,
    ){
        val useOptions = opts?: row.options.copy()
        val grid = PrettyValueGrid(hostType, row.typeToken, useOptions)
        val copied =  row.copyRow(row.typeToken)
        grid.addRow(copied)
        if(property != null){
            grid.singleLoader.setProperty(property)
        }
        if(listProperty != null){
            grid.listLoader.setProperty(listProperty)
        }
        addRenderBlock(grid)
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
    )  = useRowTemplate(row, property)

    @JvmName("useListTemplate")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        orientation: Orientation? = null,
    ) {
      return  if(orientation != null){
            useRowTemplate(row, property = null, listProperty = property,  RowOptions(orientation).noEdit())
        }else{
            useRowTemplate(row, property = null, listProperty = property)
        }
    }


    private fun <V: Any> buildByGridTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        val container = TemplateGridContainer(hostType, grid.type)
        container.initializeByGrid(grid)
        if(property != null){
            container.setProperty(property)
        }
        if(listProperty != null){
            container.setProperty(listProperty)
        }
        builder.invoke(container)
        val valueGrid = container.createValueGrid()
        addRenderBlock(valueGrid)
        templateResolved.trigger(valueGrid)
        return valueGrid
    }

    @PrettyDSL
    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> = buildByGridTemplate(grid, property, listProperty = null, builder)



    fun <V: Any> useListTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, List<V>>,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> = buildByGridTemplate(grid, property = null, listProperty = property, builder)


    private fun <V: Any> buildTemplateByRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        val inputGrid = PrettyValueGrid(hostType, row.typeToken)
        inputGrid.addRow(row.copyRow(row.typeToken))
        val container = TemplateGridContainer(inputGrid)
        if(property != null){
            container.setProperty(property)
        }
        if(listProperty != null){
            container.setProperty(listProperty)
        }
        builder.invoke(container)
        val valueGrid = container.createValueGrid()
        addRenderBlock(valueGrid)
        templateResolved.trigger(valueGrid)
        return valueGrid
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> = buildTemplateByRow(row, property, builder = builder)

    @JvmName("useRowListTemplate")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        listProperty: KProperty1<T, List<V>>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> = buildTemplateByRow(row, listProperty = listProperty, builder = builder)


    fun <V: Any> useTemplate(
        rowContainer: RowContainer<V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyGrid<V> {
        val container = TemplateGridContainer(hostType, rowContainer.type)
        container.initializeByContainer(rowContainer)
        builder.invoke(container)
        val grid = container.createGrid()
        addGridBlock(grid)
        return grid
    }

    companion object{
        fun <T: Any, V: Any> buildGridCopying(
            container: GridContainerBase<T, V>,
            opts: RowOptions? = null,
            builder: (GridContainer<V>)-> Unit
        ): PrettyGrid<V> {
            val gridContainer = GridContainer(container.type)
            gridContainer.singleLoader.initValueFrom(container.singleLoader)
            gridContainer.listLoader.initValueFrom(container.listLoader)
            builder.invoke(gridContainer)
            return gridContainer.createGrid(opts)
        }
    }
}


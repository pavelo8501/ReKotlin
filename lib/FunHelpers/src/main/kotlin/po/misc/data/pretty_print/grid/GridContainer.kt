package po.misc.data.pretty_print.grid

import po.misc.data.output.HighLight
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.grid.GridKey
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.grid.RenderableMap
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
    var options: RowOptions = RowOptions()
): GridContainerBase<T, T>(type, type), RowBuilderScope<T>{

    override val grid: PrettyGrid<T> = PrettyGrid(type, options)

    val renderMap: RenderableMap<T> = RenderableMap(hostType)
    override val rows: List<PrettyRow<T>> get() = renderMap.rows

    val foreignSize : Int get() = renderMap.foreignSize
    val renderSize: Int get() = renderMap.renderSize


    @PublishedApi
    internal fun initGrid(opts: RowOptions? = null):PrettyGrid<T>{
        grid.renderMap.populateBy(renderMap)
        if(opts != null){
            grid.applyOptions(opts)
        }
        return grid
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
        builder.invoke(rowContainer)
        rowContainer.initByGridContainer(this)
        singleLoader.info().output(HighLight)
        val container = GridValueContainer(rowContainer)
        val grid = container.initGrid(null)
        renderMap.addRenderElement(grid)
    }

    @PrettyDSL
    inline fun <reified V: Any> buildListRow(
        property: KProperty1<T, List<V>>,
        opt: CommonRowOptions? = null,
        noinline builder: RowValueContainer<T, V>.() -> Unit
    ){
        val optOverwrite =  PrettyHelper.toRowOptionsOrNull(opt)
        if(!options.sealed  && optOverwrite != null){
            options.applyChanges(optOverwrite)
        }
        val type = TypeToken.create<V>()
        val rowContainer = RowValueContainer(hostType,type, options)
        rowContainer.setProperty(property)
        builder.invoke(rowContainer)
        val container =  GridValueContainer(rowContainer)
        val grid = container.initGrid(null)
        renderMap.addRenderElement(grid)
    }

    override fun addRow(row: PrettyRow<T>): GridKey {
        return renderMap.addRenderElement(row)
    }

    @PrettyDSL
    fun buildRow(
        rowOptions: CommonRowOptions? = null,
        builder: RowContainer<T>.() -> Unit
    ){
        val options = PrettyHelper.toRowOptionsOrNull(rowOptions)
        options?.noEdit()
        val container = createRowContainer(type, options)
        container.initSignals(singleLoader)
        builder.invoke(container)
        val row =  container.initRow()
        addRow(row)
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
                renderMap.addRenderElement(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val casted = grid.castOrThrow<PrettyValueGrid<T, V>>()
                casted.singleLoader.setProperty(property)
                renderMap.addRenderElement(casted)
                return casted
            }
        }
    }



    @JvmName("useTemplateByGridList")
    fun <V: Any> useTemplate(
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
                renderMap.addRenderElement(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setProperty(property)
                renderMap.addRenderElement(valueGridCopy)
                return valueGridCopy
            }
        }
    }


    fun <V: Any> useTemplateProviding(
        grid: PrettyGridBase<*, V>,
        provider: ()-> V
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGrid(type,  provider, optionCopy)
                renderMap.addRenderElement(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid ->{
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.singleLoader.setProvider(provider)
                renderMap.addRenderElement(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    @JvmName("useTemplateByGridProviderList")
    fun <V: Any> useTemplateProviding(
        grid: PrettyGridBase<*, V>,
        orientation: Orientation,
        provider: () -> List<V>
    ): PrettyValueGrid<T, V> {
        val optionCopy = grid.options.copy(noEdit = true)
        optionCopy.orientation = orientation
        when(grid){
            is PrettyGrid<*> -> {
                val casted = grid.castOrThrow<PrettyGrid<V>>()
                val valueGrid = casted.toValueGridList(type, provider, optionCopy)
                renderMap.addRenderElement(valueGrid)
                return valueGrid
            }
            is PrettyValueGrid -> {
                val valueGrid = grid.castOrThrow<PrettyValueGrid<T, V>>()
                val valueGridCopy = valueGrid.copy(optionCopy)
                valueGridCopy.listLoader.setProvider(provider)
                renderMap.addRenderElement(valueGridCopy)
                return valueGridCopy
            }
        }
    }

    private fun <V: Any> buildByGridTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ){
        val container = TemplateGridContainer(hostType, grid.type)
        container.initializeByGrid(grid)
        if(property != null){
            container.source.setProperty(property)
        }
        if(listProperty != null){
            container.source.setProperty(listProperty)
        }
        builder.invoke(container)
        val valueGrid = container.createValueGrid()
        renderMap.addRenderElement(valueGrid)
        templateResolved.trigger(valueGrid)
    }

    @PrettyDSL
    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, V>,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ): Unit = buildByGridTemplate(grid, property, listProperty = null, builder)


    @JvmName("useTemplateListV")
    fun <V: Any> useTemplate(
        grid: PrettyGridBase<*, V>,
        property: KProperty1<T, List<V>>,
        builder:  TemplateBuilderScope<T, V>.()-> Unit
    ): Unit = buildByGridTemplate(grid, property = null, listProperty = property, builder)


    private fun <V: Any> createGridFromRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        opts: RowOptions? = null,
    ):PrettyValueGrid<T, V>{
        val useOptions = opts?: row.options.copy()
        val grid = PrettyValueGrid(hostType, row.hostType, useOptions)
        val copied =  row.copyRow(row.hostType)
        grid.addRow(copied)
        if(property != null){
            grid.singleLoader.setProperty(property)
        }
        if(listProperty != null){
            grid.listLoader.setProperty(listProperty)
        }
        renderMap.addRenderElement(grid)
        return grid
    }

    private fun <V: Any> buildTemplateByRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ) {
        val inputGrid = createGridFromRow(row, property, listProperty)
        val container = TemplateGridContainer(inputGrid)
        builder.invoke(container)
        val valueGrid = container.createValueGrid()
        renderMap.addRenderElement(valueGrid)
        templateResolved.trigger(valueGrid)
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
    ){
      createGridFromRow(row, property)
    }

    @JvmName("useTemplateFromRowList")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        orientation: Orientation? = null,
    ) {
        if (orientation != null) {
            createGridFromRow(row, property = null, listProperty = property, RowOptions(orientation).noEdit() )
        } else {
            createGridFromRow(row, property = null, listProperty = property)
        }
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): Unit = buildTemplateByRow(row, property, builder = builder)

    @JvmName("useTemplateFromRowListBuilding")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        listProperty: KProperty1<T, List<V>>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ) = buildTemplateByRow(row, listProperty = listProperty, builder = builder)


    fun <V: Any> useTemplate(
        rowContainer: RowContainer<V>,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ): PrettyGrid<V> {
        val container = TemplateGridContainer(hostType, rowContainer.type)
        container.initializeByContainer(rowContainer)
        builder.invoke(container)
        val grid = container.createGrid()
        renderMap.addRenderElement(grid)
        return grid
    }


    companion object {
        fun <T: Any, V: Any> buildGridCopying(
            container: GridContainerBase<T, V>,
            opts: RowOptions? = null,
            builder: (GridContainer<V>)-> Unit
        ): PrettyGrid<V> {
            val gridContainer = GridContainer(container.type)
            gridContainer.singleLoader.initValueFrom(container.singleLoader)
            gridContainer.listLoader.initValueFrom(container.listLoader)
            builder.invoke(gridContainer)
            return gridContainer.initGrid(opts)
        }
    }
}


package po.misc.data.pretty_print.grid

import po.misc.callbacks.CallableEventBase
import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
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
import po.misc.data.pretty_print.parts.rows.RowParams
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

    private var builtGrid: PrettyGrid<T>? = null

    internal fun createOrReinitGrid(opts: RowOptions?, gridToReinit:  PrettyGrid<T>? = null): PrettyGrid<T> {
        val reinitializing: Boolean = gridToReinit != null
        val grid =  gridToReinit?: PrettyGrid(type)
        grid.gridMap = gridMap
        grid.renderMapBacking = renderMapBacking
        grid.rowsBacking.addAll(rows)
        if(!reinitializing){
            grid.singleLoader.initValueFrom(singleLoader)
            grid.listLoader.initValueFrom(listLoader)
            grid.applyOptions(opts)
            beforeGridRender.relay(grid.beforeGridRender, CallableEventBase.RelayStrategy.MOVE)
        }
        builtGrid = grid
        return grid
    }

    @PublishedApi
    internal fun applyBuilder(buildr: GridContainer<T>.()-> Unit): PrettyGrid<T> {
        buildr.invoke(this)
        val prettyGrid: PrettyGrid<T> = createOrReinitGrid(options)
        return prettyGrid
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
        val grid = container.createValueGrid(null)
        addRenderBlock(grid)
    }


    override fun addRow(row: PrettyRow<T>): GridKey {
        rowsBacking.add(row)
        val key = addRenderBlock(row)
        if(builtGrid != null) {
            createOrReinitGrid(null, builtGrid)
        }
        return key
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


    fun <V: Any> useTemplateProviding(
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
        addRenderBlock(valueGrid)
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
        return grid
    }

    private fun <V: Any> buildTemplateByRow(
        row: PrettyRow<V>,
        property: KProperty1<T, V>? = null,
        listProperty: KProperty1<T, List<V>>? = null,
        builder:  TemplateGridContainer<T, V>.()-> Unit
    ) {
//        val inputGrid = PrettyValueGrid(hostType, row.typeToken)
//        inputGrid.addRow(row.copyRow(row.typeToken))
        val inputGrid = createGridFromRow(row, property, listProperty)
        val container = TemplateGridContainer(inputGrid)
//        if(property != null){
//            container.source.setProperty(property)
//        }
//        if(listProperty != null){
//            container.source.setProperty(listProperty)
//        }
        builder.invoke(container)
        val valueGrid = container.createValueGrid()
        addRenderBlock(valueGrid)
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
        addGridBlock(grid)
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
            return gridContainer.createOrReinitGrid(opts)
        }
    }
}


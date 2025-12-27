package po.misc.data.pretty_print.grid

import po.misc.collections.asList
import po.misc.data.output.HighLight
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.TransitionGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.dsl.PrettyDSL
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.data.pretty_print.toContainer
import po.misc.data.pretty_print.toProvider
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KProperty1


class HostGridBuilder<T>(
    type: TypeToken<T>,
    gridID: GridID? = null,
): GridBuilderBase<T, T>(type, type){

    override val prettyGrid: PrettyGrid<T> = PrettyGrid(type, gridID = gridID,)

    val rows: MutableList<PrettyRow<T>>  = mutableListOf()
    override val dataLoader: DataLoader<T, T> = DataLoader("GridContainer", type, type)

    fun finalizeGrid(sourceContainer: HostGridBuilder<*>?): PrettyGrid<T>{
        prettyGrid.verbosity = verbosity
        return prettyGrid
    }

    override fun addRow(row: PrettyRow<T>): PrettyRow<T> {
        prettyGrid.addRow(row)
        return row
    }

    @PrettyDSL
    fun buildRow(
        rowId: RowID? = null,
        builder: RowBuilder<T>.()-> Unit
    ): PrettyRow<T> {
        val rowBuilder = dslEngine.prepareRow(type, rowId, builder)
        val row =  rowBuilder.finalizeRow(this)
        return addRow(row)
    }

    @PrettyDSL
    inline fun <reified V> buildRow(
        prop:KProperty1<T, V>,
        rowId: RowID? = null,
        builder: ValueRowBuilder<T, V>.()-> Unit
    ): PrettyRow<V> {
        val rowBuilder = dslEngine.prepareRow(type,  prop,  rowId, builder)
        val row =  rowBuilder.finalizeRow(this)
        return row
    }

    @PrettyDSL
    inline fun <reified V> buildListRow(
        prop:KProperty1<T, List<V>>,
        rowId: RowID? = null,
        noinline builder: ValueRowBuilder<T, V>.()-> Unit
    ): PrettyRow<V> {
        val rowBuilder = dslEngine.prepareListRow(type,  prop,  rowId, builder)
        val row =  rowBuilder.finalizeRow(this)
        return row
    }

    @PrettyDSL
    inline fun <reified V> buildGrid(
        property: KProperty1<T, V>,
        gridID: GridID? = null,
        builder: ValueGridBuilder<T, V>.() -> Unit
    ): PrettyValueGrid<T, V>{
        val container = dslEngine.buildGrid(type, property, gridID, builder)
        val valueGrid = container.finalizeGrid(this)
        prettyGrid.renderPlan.add(valueGrid)
        return valueGrid
    }

    fun <V> useTemplate(
        container: ValueGridBuilder<T, V>,
        opt: CommonRowOptions? = null,
    ): PrettyValueGrid<T, V> {
        opt?.let {
            container.applyOptions(it)
        }
        container.dataLoader.info().output()
        val valueGrid = container.finalizeGrid()
        prettyGrid.renderPlan.add(valueGrid)
        return valueGrid
    }

    fun <V> useTemplate(
        container: ValueGridBuilder<T, V>,
        opt: CommonRowOptions? = null,
        builder: ValueGridBuilder<T, V>.() -> Unit
    ): PrettyValueGrid<T, V> {
        container.applyOptions(opt)
        builder.invoke(container)
        val valueGrid = container.finalizeGrid()
        prettyGrid.renderPlan.add(valueGrid)
        return valueGrid
    }


    fun <V> useTemplate(
        grid: PrettyGrid<V>,
        property: KProperty1<T, V>,
        opt: CommonRowOptions? = null,
    ): PrettyValueGrid<T, V> {
        val gridCopy = grid.copy()
        val rows = gridCopy.rows
        val opt =  PrettyHelper.toRowOptions(opt, gridCopy.options)
        val provider = property.toProvider(type, grid.valueType)
        val gridID = grid.id
        val valueGrid = if(gridID is GridID){
            PrettyValueGrid(provider, rows,opt, gridID)
        }else{
            PrettyValueGrid(provider, rows, opt)
        }
        prettyGrid.renderPlan.add(valueGrid)
        return valueGrid
    }

    @JvmName("useTemplateSingleBuilder")
    fun <V> useTemplate(
        grid: PrettyGrid<V>,
        property: KProperty1<T, V>,
        opt: CommonRowOptions? = null,
        builder: TransitionContainer<T, V>.() -> Unit
    ): TransitionGrid<T, V> {

        val gridCopy =  grid.copy()
        gridCopy.options =  PrettyHelper.toRowOptions(opt, gridCopy.options)
        val provider = property.toProvider(type, grid.valueType)
        val container = TransitionContainer(provider, gridCopy)
        builder.invoke(container)
        return container.finalizeGrid(this)
    }

    fun <V> useTemplate(
        grid: PrettyValueGrid<T,  V>,
        opt: CommonRowOptions? = null,
    ): PrettyValueGrid<T, V> {
        val gridCopy = grid.copy()
        gridCopy.options = PrettyHelper.toRowOptions(opt, gridCopy.options)
        prettyGrid.renderPlan.add(gridCopy)
        //renderPlan.add(gridCopy)
        return gridCopy
    }

    fun <V> useTemplate(
        grid: PrettyGrid<V>,
        property: KProperty1<T, List<V>>,
        opt: CommonRowOptions? = null,
        builder: TransitionContainer<T, V>.() -> Unit
    ): TransitionGrid<T, V> {
        val provider = property.toProvider(type, grid.valueType)
        val container =  grid.toContainer(provider, opt)
        builder.invoke(container)
        val transition = container.finalizeGrid(this)
        return transition
    }

    fun <V> useTemplate(
        grid: PrettyGrid<V>,
        property: KProperty1<T, List<V>>,
        opt: CommonRowOptions? = null,
    ): TransitionGrid<T, V> {
        val gridCopy  =  grid.copy()
        gridCopy.options  = PrettyHelper.toRowOptions(opt, gridCopy.options)
        val provider = property.toProvider(type, grid.valueType)
        val transition = TransitionGrid(provider, gridCopy)
        prettyGrid.renderPlan.add(transition)
        return transition
    }


    inline fun <reified V> useTemplate(
        grid: PrettyValueGrid<V, *>,
        property: KProperty1<T, V>,
        opt: CommonRowOptions? = null,
    ): PrettyValueGrid<T, V> {
        val provider = property.toProvider(type)
        val gridCopy = grid.copy()
        gridCopy.options =  PrettyHelper.toRowOptions(opt, gridCopy.options)
        val container = ValueGridBuilder<T, V>(provider)
        val valueContainer =  container.finalizeGrid(this)
        return valueContainer.castOrThrow()
    }

    fun <V: Any> useTemplate(
        valueRowBuilder: ValueRowBuilder<T, V>,
        gridID: GridID? = null,
    ):PrettyValueGrid<T, V>{
        val row = valueRowBuilder.finalizeRow(this)
        val grid = PrettyValueGrid(valueRowBuilder.dataProvider, row.asList(), null,  gridID)
        prettyGrid.renderPlan.add(grid)
        return grid
    }

    fun <V: Any> useTemplate(
        valueRowBuilder: ValueRowBuilder<T, V>,
        gridID: GridID? = null,
        builder: ValueGridBuilder<T, V>.() -> Unit
    ):PrettyValueGrid<T, V>{
        val valueGridBuilder = ValueGridBuilder(valueRowBuilder.dataProvider, gridID =  gridID)
        builder.invoke(valueGridBuilder)
        val row = valueRowBuilder.finalizeRow(this)
        val grid =  valueGridBuilder.finalizeGrid(row)
        prettyGrid.renderPlan.add(grid)
        return grid
    }

    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, V>,
        opt: CommonRowOptions? = null,
        gridID: GridID? = null,
        extraRow: PrettyRow<V>? = null,
    ):PrettyValueGrid<T, V>{
        val provider = property.toProvider(hostType, row.typeToken)
        val rows  = mutableListOf(row.copy(opt))
        extraRow?.let {
            rows.add(it)
        }
        val grid = PrettyValueGrid(provider, rows, opt, gridID)
        grid.dataLoader.setProperty(property)
        prettyGrid.renderPlan.add(grid)
        return grid
    }


    @JvmName("useTemplateRowList")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        opt: CommonRowOptions? = null,
        gridID:GridID? = null,
        extraRow: PrettyRow<V>? = null,
    ):PrettyValueGrid<T, V>{
        val rowCopy = row.copy(opt)
        val copiedRows  = mutableListOf(rowCopy)
        extraRow?.let {
            copiedRows.add(it)
        }
        val provider = property.toProvider(hostType, rowCopy.typeToken)
        val grid = PrettyValueGrid(provider, copiedRows, opt, gridID)
        prettyGrid.renderPlan.add(grid)

        return grid
    }

    @JvmName("useTemplateRowListVariance")
    fun <V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        orientation: Orientation,
        gridID: GridID? = null,
    ):PrettyValueGrid<T, V> = useTemplate(row, property, RowOptions(orientation), gridID)


    @JvmName("useTemplateRowListBuilder")
    inline fun <reified V: Any> useTemplate(
        row: PrettyRow<V>,
        property: KProperty1<T, List<V>>,
        gridID: GridID? = null,
        builder: ValueGridBuilder<T, V>.() -> Unit
    ):PrettyValueGrid<T, V>{
        val copiedRows  = mutableListOf(row.copy())
        val dataProvider =  property.toProvider(type, tokenOf<V>())
        val container = ValueGridBuilder(dataProvider,copiedRows, gridID = gridID)
        builder.invoke(container)
        val builtGrid =  container.finalizeGrid(this)
        prettyGrid.renderPlan.add(builtGrid)
        return  builtGrid
    }

    companion object {
        @PublishedApi
        internal val dslEngine: DSLEngine = DSLEngine()
   }
}

package po.misc.data.pretty_print.grid

import po.misc.collections.asList
import po.misc.data.pretty_print.parts.GridKey
import po.misc.data.pretty_print.parts.GridSource
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowOptionsEditor
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

class TemplateGridContainer<T: Any, V: Any>(
    hostTypeToken: TypeToken<T>,
    private val sourceGrid: GridValueContainer<T, V>,
    val options: RowOptionsEditor = RowOptions()
): GridContainerBase<T, V>(hostTypeToken, sourceGrid.typeToken), RowOptionsEditor by options{

    constructor(
        hostToken: TypeToken<T>,
        sourceRow: PrettyRow<V>
    ):this(
        hostToken,
        GridValueContainer(hostToken,  sourceRow.typeToken).also { it.addRow(sourceRow) }
    )

    internal var pluggedKey:  GridKey? = null

    private fun loadValueContainer(): GridValueContainer<T, V>{
        val key = pluggedKey
        if(key != null){
            sourceGrid.insertRows(key.order, rows)
        }else{
            sourceGrid.insertRows(0, rows)
        }
        return sourceGrid
    }

    @PublishedApi
    internal fun applyBuilder(
        property: KProperty1<T, V>,
        builder: TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        builder.invoke(this)
        val valueContainer = loadValueContainer()
        val grid = valueContainer.createValueGrid()
        if(options.edited){
            grid.applyOptions(options as RowOptions)
        }
        grid.singleLoader.setReadOnlyProperty(property)
        return grid
    }

    @PublishedApi
    @JvmName("applyBuilderList")
    internal fun applyBuilder(
        property: KProperty1<T, List<V>>,
        builder: TemplateGridContainer<T, V>.()-> Unit
    ): PrettyValueGrid<T, V> {
        builder.invoke(this)
        val valueContainer = loadValueContainer()
        val grid = valueContainer.createValueGrid()
        if(options.edited){
            grid.applyOptions(options as RowOptions)
        }
        grid.listLoader.setReadOnlyProperty(property)
        return grid
    }

    fun buildGrid(
        builder: TemplateGridContainer<T, V>.()-> Unit
    ): PrettyGrid<V> {
        builder.invoke(this)
        val valueContainer = loadValueContainer()
        return  if(options.edited){
            valueContainer.createGrid(options as RowOptions)
        }else{
            valueContainer.createGrid()
        }
    }

    fun renderHere(){
        if(pluggedKey == null){
            rows.lastIndex
            pluggedKey =  GridKey(rows.lastIndex + 1, GridSource.Renderable)
        }
    }

    companion object

}
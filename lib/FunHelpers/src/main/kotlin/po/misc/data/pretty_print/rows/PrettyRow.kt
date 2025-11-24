package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.Console80
import po.misc.data.pretty_print.RenderDefaults
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.styles.SpecialChars
import po.misc.reflection.NameValuePair
import kotlin.reflect.KProperty1

/**
 * Represents a row of formatted cells that can render an ordered list of values.
 *
 * Typical usage:
 * ```
 * val row = PrettyRow(
 *     PrettyCell(10, KeyPreset),
 *     PrettyCell(20, ValuePreset)
 * )
 * println(row.render(listOf("Name", "John Doe")))
 * ```
 *
 * Extra values exceeding the number of cells are appended after the row
 * using [outsideCellSeparator].
 */
class PrettyRow(
    private val initialCells: List<PrettyCellBase<*>>,
    val separator: String = " | ",
){

    enum class InitType{ Manual, Property }

    var defaults: RenderDefaults = Console80

    var initType: InitType = InitType.Manual
    var outsideCellSeparator: String = " : "


    constructor(vararg cells: PrettyCellBase<*>):this(cells.toList()){
        SpecialChars.CELL_SEPARATOR
    }

    constructor(separator: String = " | ", vararg cells: PrettyCellBase<*>):this(cells.toList(), separator)

    constructor(container: PrettyDataContainer):this(container.prettyCells, SpecialChars.CELL_SEPARATOR){
        cells = container.prettyCells
    }

    var cells: List<PrettyCellBase<*>>
        internal set

    init {
        cells = computeFormattedCells()
    }

    fun submitCells(list: PrettyCell, initType: InitType){
        this.initType = initType
        cells = computeFormattedCells()
    }

    private fun computeFormattedCells(): List<PrettyCellBase<*>> {
        val copied = initialCells.map { cell ->
            val safeWidth = cell.width.coerceAtMost( defaults.DEFAULT_WIDTH)
            val cellCopy =  when(cell){
                is PrettyCell -> cell.builder(safeWidth, cell.align)
                is KeyedCell -> cell.builder(safeWidth, cell.cellName)

            }
            PrettyCellBase.copyKeyParams(cell, cellCopy)
        }
        return copied
    }

    /**
     * Renders a row by applying each value to its cell.
     *
     * `values.size > cells.size` → extra values rendered after the row.
     */
    fun render(values: List<String>): String {
        val cellCount = cells.size
        val result = StringBuilder()
        for (i in 0 until cellCount) {
            if (i >= values.size) break
                result.append(cells[i].render(values[i]))
            if (i < cellCount - 1)
                result.append(separator)
        }
        if (values.size > cellCount) {
            var  tail = outsideCellSeparator
            tail += values.drop(cellCount).joinToString(" ")
            result.append(" ").append(tail)
        }
        return result.toString()
    }
    fun render(nameValuePair: NameValuePair): String = render(listOf(nameValuePair.name, nameValuePair.value))
    fun render(vararg values: String): String = render(values.toList())
    fun render(vararg values: Any): String = render(values.map { it.toString() })

    fun <T: Any> render(receiver: T): String {
        val withProperties = cells.filterIsInstance<KeyedCell>().filter { it.property != null }
        val properties = withProperties.map { it.property }.filterIsInstance<KProperty1<Any, *>>()
        val resultList = properties.map { it.get(receiver).toString() }
        return render(resultList)
    }
}
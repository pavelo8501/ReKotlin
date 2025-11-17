package po.misc.data.pretty_print

import po.misc.data.styles.SpecialChars


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
    val cells: List<PrettyCell>,
    val separator: String = " | ",
    val defaults: RenderDefaults = Console80
){
    var outsideCellSeparator: String = " : "

    constructor(
        separator: String = " | ",
        defaults: RenderDefaults = Console80,
        vararg cells: PrettyCell,
    ):this(cells.toList(), separator, defaults)

    constructor(
        vararg cells: PrettyCell,
        separator: String  = " | ",
    ):this(cells.toList(), separator, Console80)

    constructor(
        defaults: RenderDefaults = Console80,
        vararg cells: PrettyCell,
    ):this(cells.toList(), SpecialChars.CELL_SEPARATOR, defaults)


    private val formattedCells: List<CellRenderer>
    init { formattedCells = computeFormattedCells() }
    private fun computeFormattedCells(): List<CellRenderer> {
        return cells.map { cell ->
            val safeWidth = cell.width.coerceAtMost(defaults.DEFAULT_WIDTH)
            val cellCopy = PrettyCell(
                width = safeWidth,
                color = cell.color,
                align = cell.align,
                defaults = defaults
            )
            PrettyCell.copyKeyParams(cell, cellCopy)
        }
    }

    /**
     * Renders a row by applying each value to its cell.
     *
     * `values.size > cells.size` → extra values rendered after the row.
     */
    fun render(values: List<String>, lastBreakLine: Boolean = true): String {
        val cellCount = formattedCells.size
        val result = StringBuilder()
        for (i in 0 until cellCount) {
            if (i >= values.size) break
            result.append(formattedCells[i].render(values[i]))
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
    fun render(vararg values: String): String = render(values.toList())

    fun render(vararg values: Any): String = render(values.map { it.toString() })

}
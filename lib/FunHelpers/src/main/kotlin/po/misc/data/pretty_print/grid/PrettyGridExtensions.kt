package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.RowConfig
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.PrettyRow

/**
 * Adds a new row to the grid that begins with a static header cell.
 *
 * This is a shorthand for creating a row where the first cell contains a
 * predefined header text (for example a section title), followed by no
 * additional cells. The row and the header cell can be customized using
 * presets.
 *
 * Typical usage:
 * ```
 * grid.addHeadedRow("User Information")
 * ```
 *
 * @param text The header text placed in the first cell of the row.
 * @param rowConfig Controls row layout and styling. Accepts either a preset
 *                  or explicit [RowOptions]. Defaults to [RowPresets.HeadedVertical].
 * @param cellPreset Defines styling for the header cell. Defaults to [PrettyPresets.Header].
 *
 * @return The created [PrettyRow] instance for further customization if needed.
 */
fun <T: Any> PrettyGridBase<T>.addHeadedRow(
    text: String,
    rowConfig: RowConfig = RowPresets.HeadedVertical,
    cellPreset: PrettyPresets = PrettyPresets.Header,
):  PrettyRow<*> {
    cellPreset.toOptions()
    val staticCell = StaticCell(text, cellPreset.toOptions())
    val realRow = PrettyRow(staticCell)
    val options = when (rowConfig) {
        is RowPresets -> rowConfig.toOptions()
        is RowOptions -> rowConfig
    }
    realRow.setRowOptions(options)
    addRenderBlock(realRow)
    return realRow
}

/**
 * Builds and adds a new row that begins with a static header cell, followed by
 * dynamically constructed cells defined in the [builder] block.
 *
 * This function is useful when a row needs a labeled header (such as a section
 * title or category field) and then additional content cells derived from
 * receiver data or static values.
 *
 * Example:
 * ```
 * grid.buildHeadedRow("Order") {
 *     addCell(Order::id)
 *     addCell(Order::total)
 * }
 * ```
 *
 * @param text The header text placed in the first cell.
 * @param rowConfig Controls row layout and styling. Can be a preset or explicit
 *                  [RowOptions]. Defaults to [RowPresets.HeadedVertical].
 * @param cellPreset Styling preset used for the header cell. Defaults to [PrettyPresets.Header].
 * @param builder Lambda constructing the remaining cells in the row using a [CellContainer].
 */
fun <T: Any> PrettyGridBase<T>.buildHeadedRow(
    text: String,
    rowConfig: RowConfig = RowPresets.HeadedVertical,
    cellPreset: PrettyPresets = PrettyPresets.Header,
    builder: CellContainer<T>.() -> Unit
){
    val constructor = CellContainer<T>(typeToken)
    cellPreset.toOptions()
    val staticCell = StaticCell(text, cellPreset.toOptions())
    constructor.storeCell(staticCell)
    builder.invoke(constructor)
    val realRow = PrettyRow(constructor)
    val options = when (rowConfig) {
        is RowPresets -> rowConfig.toOptions()
        is RowOptions -> rowConfig
    }
    realRow.setRowOptions(options)
    addRenderBlock(realRow)
}
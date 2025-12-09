package po.test.misc.data.pretty_print.parts

import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.createRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestOptionPropagation : PrettyTestBase() {

    private val rowOption = RowOptions(Orientation.Vertical)

    @Test
    fun `Options of the grid are propagated down the chain during build`() {

        val grid = buildPrettyGrid(rowOption) {
            createRow(Row.Row1, StaticCell(headerText1))
            createRow(Row.Row2, StaticCell(headerText2))
        }
        assertNotNull(grid.getRowOrNull(Row.Row1)) { row ->
            assertEquals(rowOption.orientation, row.options.orientation)
            assertNotNull(row.cells.firstOrNull()) { cell1 ->
                assertEquals(rowOption.orientation, cell1.orientation)
                assertNotEquals(Row.Row1, cell1.cellOptions.id)
            }
        }
        assertNotNull(grid.getRowOrNull(Row.Row2)) { row ->
            assertEquals(rowOption.orientation, row.options.orientation)
            assertNotNull(row.cells.firstOrNull()) { cell1 ->
                assertEquals(rowOption.orientation, cell1.orientation)
                assertNotEquals(Row.Row2, cell1.cellOptions.id)
            }
        }
    }



}
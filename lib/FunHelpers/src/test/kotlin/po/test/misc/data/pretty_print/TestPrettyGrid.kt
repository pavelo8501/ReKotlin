package po.test.misc.data.pretty_print

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.cells.cellDelete
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestPrettyGrid : PrettyTestBase() {

    val intProperty: Int = 100
    val boolProperty: Boolean = true
    val textProperty: (TestPrettyGrid) ->  String = {receiver->
       "Resolved!!!!!".colorize(Colour.Green)
    }
    @Test
    fun `Using specific template id to control render`(){
        val grid = buildPrettyGrid<TestPrettyGrid> {
            buildRow {
                addCells(::textProperty, ::intProperty, ::boolProperty)

                add(textProperty)
                cellDelete(textProperty)
            }
        }
        grid.render(this).output()

        assertNotNull(grid.rows.firstOrNull()){
            assertNotNull(it.cells.firstOrNull()){cell->
                assertIs<KeyedCell<TestPrettyGrid>>(cell)
                cell.keyText.output("Cell ->  ")
            }
        }
    }
}
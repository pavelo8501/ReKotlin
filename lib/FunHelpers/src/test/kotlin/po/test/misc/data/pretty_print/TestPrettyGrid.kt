package po.test.misc.data.pretty_print

import org.junit.jupiter.api.Test
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPrettyGrid : PrettyTestBase() {


    @Test
    fun `Using specific template id to control render`(){

        val cell1Text = "Static cell 1 on first row"
        val cell2Text = "Second Static cell on first row"
        val cell3Text = "First Static cell on second row"

        var prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Grid.Grid1, Orientation.Horizontal)) {
                addCell(cell1Text)
                addCell(cell2Text)
            }
            buildRow(RowOptions(Grid.Grid2, Orientation.Horizontal)) {
                addCell(cell3Text)
            }
        }
        val record = createRecord()
        var render = prettyGrid.render(record, RowOptions(Grid.Grid1))

        assertTrue { render.contains(cell1Text) && render.contains(cell2Text) }
        assertFalse { render.contains(cell3Text) }

        prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Grid.Grid1, Orientation.Horizontal)) {
                addCell(cell1Text)
                addCell(cell2Text, Options(Grid.Grid2))
            }
            buildRow(RowOptions(Grid.Grid2, Orientation.Horizontal)) {
                addCell(cell3Text)
            }
        }
//        render = prettyGrid.render(record, RowOptions(CellTemplate.Cell2, Template.Template1))
//
//        assertTrue {  render.contains(cell1Text) && render.contains(cell2Text) }
//        assertFalse { render.contains(cell3Text) }
//
//        render = prettyGrid.render(record, RowOptions(Template.Template1))
//        assertTrue {  render.contains(cell1Text) }
//        assertFalse { render.contains(cell2Text) && render.contains(cell3Text) }
    }
}
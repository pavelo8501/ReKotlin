package po.test.misc.data.pretty_print.grid

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.styles.Colour
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPrettyGrid : PrettyTestBase() {


    @Test
    fun `Using specific template id to control render`(){

        val cell1Text = "Static cell 1 on first row"
        val cell2Text = "Second Static cell on first row"
        val cell3Text = "First Static cell on second row"

        var prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Template.Template1, Orientation.Horizontal)) {
                addCell(cell1Text)
                addCell(cell2Text)
            }
            buildRow(RowOptions(Template.Template2, Orientation.Horizontal)){
                addCell(cell3Text)
            }
        }
        val record = createRecord()
        var render = prettyGrid.render(record, RowOptions(Template.Template1))
        assertTrue {  render.contains(cell1Text) && render.contains(cell2Text) }
        assertFalse { render.contains(cell3Text) }

        prettyGrid = buildPrettyGrid<PrintableRecord> {
            buildRow(RowOptions(Template.Template1, Orientation.Horizontal)) {
                addCell(cell1Text)
                addCell(cell2Text, CellOptions(CellTemplate.Cell2))
            }
            buildRow(RowOptions(Template.Template2, Orientation.Horizontal)){
                addCell(cell3Text)
            }
        }
        render = prettyGrid.render(record, RowOptions(Template.Template1, CellTemplate.Cell2))

        assertTrue {  render.contains(cell1Text) && render.contains(cell2Text) }
        assertFalse { render.contains(cell3Text) }

        render = prettyGrid.render(record, RowOptions(Template.Template1))
        assertTrue {  render.contains(cell1Text) }
        assertFalse { render.contains(cell2Text) && render.contains(cell3Text) }
    }
}
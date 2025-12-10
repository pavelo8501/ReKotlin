package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestTemplateRendering : PrettyTestBase() {

    private val entryRow = buildPrettyRow<PrintableElement> {
        addCells(PrintableElement::elementName, PrintableElement::parameter, PrintableElement::value)
    }

    private val record = createRecord()

    @Test
    fun `Multi composed grid rendered as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            addHeadedRow(headerText1)
            buildRow{
                addCell(PrintableRecord::name)
                addCell(PrintableRecord::description)
            }
            useTemplate(entryRow, PrintableRecord::elements){
                addHeadedRow("Sub header")
                orientation = Orientation.Vertical
                renderHere()
            }
            onValueResolved {

            }
            buildRow{
                addCell(footerText.colorize(Colour.Blue))
            }
        }

        val templatePart = grid.renderBlocks.getOrNull(2)
        assertIs< PrettyValueGrid<PrintableRecord, PrintableElement>>(templatePart)
        assertNotNull(templatePart.options)
        assertEquals(Orientation.Vertical, templatePart.options.orientation)
        grid.render(record).output()

    }
}
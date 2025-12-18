package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestTemplateRendering : PrettyTestBase() {

    private val entryRow = buildPrettyRow<PrintableElement> {
        addAll(PrintableElement::elementName, PrintableElement::parameter, PrintableElement::value)
    }

    private val record = createRecord()

    @Test
    fun `Multi composed grid rendered as expected`() {
        val grid = buildPrettyGrid<PrintableRecord> {
            headedRow(headerText1)
            buildRow{
                add(PrintableRecord::name)
                add(PrintableRecord::description)
            }
            useTemplate(entryRow, PrintableRecord::elements){
                headedRow("Sub header")
                orientation = Orientation.Vertical
                renderHere()
            }
            buildRow{
                add(footerText.colorize(Colour.Blue))
            }
        }

        val templatePart = grid.renderMap.elements.getOrNull(2)
        assertIs< PrettyValueGrid<PrintableRecord, PrintableElement>>(templatePart)
        assertNotNull(templatePart.options)
        assertEquals(Orientation.Vertical, templatePart.options.orientation)
        grid.render(record).output()

    }
}
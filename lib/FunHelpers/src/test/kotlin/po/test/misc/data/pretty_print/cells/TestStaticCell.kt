package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.misc.data.styles.colorize
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestStaticCell : PrettyTestBase(){

    private val text1 = "Text_1"
    private val text2 = "Text_2"

    private val additionalText = "Line_2"

    @Test
    fun `StaticCell string builder work as expected`(){

        val row = buildPrettyRow<PrintableRecord> {
            build{
                append(text1.colorize(Colour.Green))
                append(" ",  additionalText)
            }
        }
        val cell = assertIs<StaticCell>(row.cells.first())
        val render =  cell.render()
        render.output()
        assertTrue { render.contains(text1) }
        assertTrue { render.contains(additionalText)}
        assertTrue{ render.contains(Colour.Green.code) }
    }

    @Test
    fun `StaticCell renderer work as expected`(){
        val cell = StaticCell(text1)
        cell.colourConditions {
            Colour.Green.buildCondition {
                contains(text1)
            }
        }
        val render1 = cell.render()
        assertTrue{ render1.contains(Colour.Green.code) }
    }

    @Test
    fun `StaticCell presets applied correctly`(){
        val cell = StaticCell(text1)
        cell.applyOptions(CellPresets.Success)
        assertEquals(Align.LEFT, cell.cellOptions.alignment)
        assertEquals(TextStyle.Bold, cell.cellOptions.style.textStyle)
        assertEquals(Colour.GreenBright, cell.cellOptions.style.colour)
    }

}
package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestStaticCell {

    private val text1 = "line 1"

    @Test
    fun `StaticCell string builder work as expected`(){

        val cell = StaticCell()
        cell.buildText {
            appendLine(text1)
            appendLine("line 2")
        }
        assertTrue { cell.text.contains(text1) }
        val prettyRow = buildPrettyRow(CellReceiverContainer) {
            addCell(StaticCell){
                appendLine(text1)
                appendLine("line 2")
            }
        }
        assertNotNull(prettyRow.cells.firstOrNull()){cell->
            assertIs<StaticCell>(cell)
            assertTrue { cell.text.contains(text1) }
        }
        val cell2 = StaticCell()
        cell2.applyText {
            """
                line 1
                line 2
            """.trimIndent()
        }
        assertTrue { cell2.text.contains(text1) }
        cell2.output()
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

        val cell2 =  PrettyCellBase.copyParameters(cell, StaticCell())
        assertTrue { cell2.text.isEmpty() }
        val render2 =  cell2.render(text1)
        assertEquals(render1, render2)
    }



    @Test
    fun `StaticCell presets applied correctly`(){
        val cell = StaticCell(text1)
        cell.applyPreset(PrettyPresets.Success)
        assertEquals(Align.LEFT, cell.options.alignment)
        assertEquals(TextStyle.Bold, cell.options.styleOptions.style)
        assertEquals(Colour.GreenBright, cell.options.styleOptions.colour)
    }

}
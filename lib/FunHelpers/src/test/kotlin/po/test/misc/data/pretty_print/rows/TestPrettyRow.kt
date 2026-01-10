package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.collections.repeatBuild
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.PrettyRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestPrettyRow : PrettyTestBase(){



    private val cell1Text = "Cell text 1"
    private val cell2Text = "Cell text 2"
    private val cell3Text = "Cell text 3"
    private val cell4Text = "Cell text 4"

    private val noGapsText1 = "text_1"
    private val noGapsText2 = "text_2"
    private val noGapsText3 = "text_3"

    @Test
    fun `Horizontal render with 1 cell work as expected`(){

        val static = noGapsText1.toCell()
        val prettyRow = PrettyRow(static)
        val record = PrintableRecord()
//        val render =  prettyRow.render(record)
//        val lines = render.splitLines()
//        assertEquals(1, lines.size)
//        assertEquals(0,  render.count(SpecialChars.WHITESPACE), "Single cell should not have surrounding whitespaces")
    }

    @Test
    fun `Row vararg renderer  work as expected`(){
        var staticCells = 4.repeatBuild {
            StaticCell(emptyString)
        }
        val prettyRow = PrettyRow(staticCells)
        val render = prettyRow.renderAny(cell1Text, cell2Text)
        render.output(enableOutput)
        assertTrue { render.contains(cell1Text) && render.contains(cell2Text) }

        staticCells = 2.repeatBuild {
            StaticCell(emptyString)
        }
        prettyRow.initCells(staticCells)
        assertEquals(2, prettyRow.cells.size)
        val renderLessCells = prettyRow.renderAny(cell1Text, cell2Text, cell3Text, cell4Text)
        assertTrue { renderLessCells.contains(cell1Text) && renderLessCells.contains(cell2Text) }
        assertTrue { renderLessCells.contains(cell3Text) && renderLessCells.contains(cell4Text) }
    }

    @Test
    fun `Row list renderer work as expected`(){
        var staticCells = 4.repeatBuild {
            StaticCell(emptyString)
        }
        val prettyRow = PrettyRow(staticCells)
        var inputList =  buildList {
            add(cell1Text)
            add(cell2Text)
        }
        val render = prettyRow.renderAny(inputList)
        render.output(enableOutput)
        assertTrue { render.contains(cell1Text) && render.contains(cell2Text) }
        staticCells = 2.repeatBuild {
            StaticCell(emptyString)
        }
        inputList =  buildList {
            add(cell1Text)
            add(cell2Text)
            add(cell3Text)
            add(cell4Text)
        }
        prettyRow.initCells(staticCells)
        assertEquals(2, prettyRow.cells.size)
        val renderLessCells = prettyRow.renderAny(inputList)
        assertTrue { renderLessCells.contains(cell1Text) && renderLessCells.contains(cell2Text) }
        assertTrue { renderLessCells.contains(cell3Text) && renderLessCells.contains(cell4Text) }
    }

    @Test
    fun `Row single value renderer work as expected`() {

        val printableRecord = PrintableRecord()
        val staticCells = 2.repeatBuild {
            StaticCell(printableRecord.name)
        }
        val prettyRow = PrettyRow<PrintableRecord>()
        prettyRow.initCells(staticCells)
        val render = prettyRow.render(printableRecord)
        render.output(enableOutput)
        assertTrue { render.contains(printableRecord.name) }
    }

    @Test
    fun `Row presets work as expected`(){
        val cell1Text = "Cell text"
        val staticCells = 5.repeatBuild {
            StaticCell(emptyString)
        }
        val prettyRow = PrettyRow(staticCells)
        prettyRow.render(cell1Text, RowPresets.Vertical)
        assertEquals(Orientation.Horizontal,  prettyRow.options.orientation)
        prettyRow.applyOptions(RowPresets.Vertical)
        assertEquals(Orientation.Vertical,  prettyRow.options.orientation)
    }

}


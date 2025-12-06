package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.collections.repeatBuild
import po.misc.data.count
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyBuilder
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.splitLines
import po.misc.data.styles.SpecialChars
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestPrettyRow : PrettyTestBase(),  PrettyBuilder{

    private val cell1Text = "Cell text 1"
    private val cell2Text = "Cell text 2"
    private val cell3Text = "Cell text 3"
    private val cell4Text = "Cell text 4"

    private val noGapsText1 = "text_1"
    private val noGapsText2 = "text_2"
    private val noGapsText3 = "text_3"

    @Test
    fun `Horizontal render with 1 cell work as expected`(){

        val static = noGapsText1.toStatic()
        val prettyRow = PrettyRow(static)
        val record = PrintableRecord()
//        val render =  prettyRow.render(record)
//        val lines = render.splitLines()
//        assertEquals(1, lines.size)
//        assertEquals(0,  render.count(SpecialChars.WHITESPACE), "Single cell should not have surrounding whitespaces")
    }

    @Test
    fun `Horizontal render with 2 cells work as expected`(){
        val static = noGapsText1.toStatic()
        val static2 = noGapsText2.toStatic()
        val record = PrintableRecord()
        val prettyRow = PrettyRow(static, static2)
//        val render =  prettyRow.render(record)
//        val lines = render.splitLines()
//        assertEquals(1, lines.size)
//        assertEquals(1, render.count{ it == '|' }, "Two cell render should have 1 separator" )
//        assertEquals(2,  render.count(SpecialChars.WHITESPACE), "Two cell render should have 2 spaces")
    }

    @Test
    fun `Horizontal render with multiple cells work as expected`(){

        val static : StaticCell = noGapsText1.toStatic()
        val static2 = noGapsText2.toStatic()
        val static3 = noGapsText3.toStatic()

        val record = PrintableRecord()
        val prettyRow = PrettyRow(static, static2, static3)
//        val render =  prettyRow.render(record)
//        val lines = render.splitLines()
//        render.output()
//        assertEquals(1, lines.size)
//        assertEquals(2, render.count{ it == '|' }, "3 cell render should have 2 separator" )
//        assertEquals(4,  render.count(SpecialChars.WHITESPACE), "3 cell render should have 4 spaces")
    }

    @Test
    fun `Row vararg renderer  work as expected`(){
        var staticCells = 4.repeatBuild {
            StaticCell()
        }
        val prettyRow = PrettyRow(staticCells)
        val render = prettyRow.renderAny(cell1Text, cell2Text)
        assertTrue { render.contains(cell1Text) && render.contains(cell2Text) }

        staticCells = 2.repeatBuild {
            StaticCell()
        }
        prettyRow.setCells(staticCells)
        assertEquals(2, prettyRow.cells.size)
        val renderLessCells = prettyRow.renderAny(cell1Text, cell2Text, cell3Text, cell4Text)
        assertTrue { renderLessCells.contains(cell1Text) && renderLessCells.contains(cell2Text) }
        assertTrue { renderLessCells.contains(cell3Text) && renderLessCells.contains(cell4Text) }
    }

    @Test
    fun `Row list renderer work as expected`(){
        var staticCells = 4.repeatBuild {
            StaticCell()
        }
        val prettyRow = PrettyRow(staticCells)
        var inputList =  buildList {
            add(cell1Text)
            add(cell2Text)
        }
        val renderList = prettyRow.render(inputList)
        assertTrue { renderList.contains(cell1Text) && renderList.contains(cell2Text) }
        staticCells = 2.repeatBuild {
            StaticCell()
        }
        inputList =  buildList {
            add(cell1Text)
            add(cell2Text)
            add(cell3Text)
            add(cell4Text)
        }
        prettyRow.setCells(staticCells)
        assertEquals(2, prettyRow.cells.size)
        val renderLessCells = prettyRow.render(inputList)
        assertTrue { renderLessCells.contains(cell1Text) && renderLessCells.contains(cell2Text) }
        assertTrue { renderLessCells.contains(cell3Text) && renderLessCells.contains(cell4Text) }
    }

    @Test
    fun `Row single value renderer work as expected`(){

        val printableRecord = PrintableRecord()
        val  staticCells = 2.repeatBuild {
            StaticCell(printableRecord.name)
        }
//        val prettyRow = PrettyRow(staticCells)
//        val render = prettyRow.render(printableRecord)
//        assertTrue { render.contains(printableRecord.name) }
    }

    @Test
    fun `Cells border rendering logic`(){
        val cell1Text = "Cell 1 text"
        val cell2Text = "Cell 2 text"
        val cell1 = StaticCell(cell1Text)
        val cell2 = StaticCell(cell2Text)
        val row = PrettyRow(cell1, cell2)
        val renderedText = row.renderAny(cell1Text, cell2Text)
        assertTrue { renderedText.contains(cell1Text) && renderedText.contains(cell2Text) }
        val bordersCount = renderedText.count{ it == '|' }
        assertEquals(1, bordersCount)
    }

    @Test
    fun `Row presets work as expected`(){
        val cell1Text = "Cell text"
        val staticCells = 5.repeatBuild {
            StaticCell()
        }
        val prettyRow = PrettyRow(staticCells)
        prettyRow.render(cell1Text, RowPresets.Vertical)
        assertEquals(Orientation.Horizontal,  prettyRow.options.orientation)
        prettyRow.applyOptions(RowPresets.Vertical)
        assertEquals(Orientation.Vertical,  prettyRow.options.orientation)
    }

    @Test
    fun `Building row by multiple entries`(){
        val prettyRow = buildPrettyRow<PrintableRecord> {
           // addCells(PrintableRecord::name, PrintableRecord::component)
        }
        assertEquals(2, prettyRow.cells.size)
    }

}


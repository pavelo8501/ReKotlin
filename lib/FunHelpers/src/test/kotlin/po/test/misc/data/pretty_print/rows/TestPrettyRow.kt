package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.collections.repeatBuild
import po.misc.context.component.Component
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestPrettyRow : Component{


    private class PrintableRecord(
        val name: String = "PersonalName",
        val component: String = "Component name ",
        val description: String = "Some description of the component",
    ): PrettyPrint {
        override val formattedString: String get() = "name : $name component: $component description : $description"

        override fun toString(): String {
           return "$name $component $description"
        }
    }

    private val cell1Text = "Cell text 1"
    private val cell2Text = "Cell text 2"
    private val cell3Text = "Cell text 3"
    private val cell4Text = "Cell text 4"

    @Test
    fun `Row vararg renderer  work as expected`(){
        var staticCells = 4.repeatBuild {
            StaticCell()
        }
        val prettyRow = PrettyRow(staticCells)
        val render = prettyRow.render(cell1Text, cell2Text)
        assertTrue { render.contains(cell1Text) && render.contains(cell2Text) }

        staticCells = 2.repeatBuild {
            StaticCell()
        }
        prettyRow.setCells(staticCells)
        assertEquals(4, prettyRow.cells.size)
        val renderLessCells = prettyRow.render(cell1Text, cell2Text, cell3Text, cell4Text)
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
        assertEquals(4, prettyRow.cells.size)
        val renderLessCells = prettyRow.render(inputList)
        assertTrue { renderLessCells.contains(cell1Text) && renderLessCells.contains(cell2Text) }
        assertTrue { renderLessCells.contains(cell3Text) && renderLessCells.contains(cell4Text) }
    }

    @Test
    fun `Row single value renderer work as expected`(){

        val printableRecord = PrintableRecord()
        val  staticCells = 2.repeatBuild {
            StaticCell()
        }
        val prettyRow = PrettyRow(staticCells)
        val render = prettyRow.render(printableRecord)
        assertTrue { render.contains(printableRecord.name) }
    }

    @Test
    fun `Cells border rendering logic`(){
        val cell1Text = "Cell 1 text"
        val cell2Text = "Cell 2 text"
        val cell1 = StaticCell(cell1Text)
        val cell2 = StaticCell(cell2Text)
        val row = PrettyRow(cell1, cell2)
        val renderedText = row.render(cell1Text, cell2Text)
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
        prettyRow.render(cell1Text, RowPresets.VerticalRow)
        assertEquals(PrettyRow.Orientation.Horizontal,  prettyRow.options.orientation)
        prettyRow.applyPreset(RowPresets.VerticalRow)
        assertEquals(PrettyRow.Orientation.Vertical,  prettyRow.options.orientation)

    }
}


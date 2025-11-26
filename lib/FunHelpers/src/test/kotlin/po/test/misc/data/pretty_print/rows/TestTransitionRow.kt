package po.test.misc.data.pretty_print.rows

import org.junit.jupiter.api.Test
import po.misc.data.count
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.data.splitLines
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals

class TestTransitionRow {

    private class PrintableRecordSubClass(
        val subName: String = "PrintableRecordSubClass",
        val subComponent: String = "PrintableRecordSubClassComponent",
    )

    private class PrintableRecord(
        val name: String = "PersonalName",
        val component: String = "Component name ",
        val description: String = "Some description of the component",
        val subClass: PrintableRecordSubClass = PrintableRecordSubClass()
    )

    @Test
    fun `Transition row rendered as expected`() {

        val prettyGrid = PrettyGrid(TypeToken.create<PrintableRecord>())
        val staticCell = StaticCell("Some text")
        val prettyRow = PrettyRow(staticCell)

        val staticCellOnTransitional = StaticCell("Some static cell on transitional")
        val transitional = TransitionRow(TypeToken.create<PrintableRecordSubClass>(), staticCellOnTransitional)
        prettyGrid.addRow(prettyRow)
        prettyGrid.addRow(transitional)

        val record = PrintableRecord()
        transitional.provideTransition {
            record.subClass
        }
        val render =  prettyGrid.render(record)
        val lines = render.splitLines()
        assertEquals(1, transitional.cells.size)
        assertEquals(2, lines.size)
    }

    @Test
    fun `Vertical row rendered as expected`() {
        val record = PrintableRecord()
        val row = TransitionRow.buildRow(PrintableRecord::subClass, PrintableRecord::class, RowPresets.VerticalRow) {
            addCell(PrintableRecordSubClass::subName)
            addCell(PrintableRecordSubClass::subComponent)
        }
        val render = row.render(record.subClass)
        assertEquals(2, row.cells.size)
        assertEquals(1, row.cells.last().index)
        val split = render.splitLines()
        assertEquals(2, split.size)
        split.forEach {
            assertEquals(1, it.count(SpecialChars.WHITESPACE))
        }
    }

}
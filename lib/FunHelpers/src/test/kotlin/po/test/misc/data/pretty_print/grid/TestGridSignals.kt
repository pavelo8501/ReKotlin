package po.test.misc.data.pretty_print.grid

import po.misc.data.output.Identify
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.grid.GridParams
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.test.misc.data.TestPrintableTemplate
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridSignals : PrettyTestBase(){

    private val subRow = buildPrettyRow<PrintableElement>(Row.SubTemplateRow) {
        add(PrintableElement::elementName)
    }

    private val record = createRecord()

    @Test
    fun `Signal propagation work as expected`(){
        val result = mutableListOf<GridParams>()
        val grid = buildPrettyGrid<PrintableRecord> {
            buildRow {
                add("Static")
            }
            useTemplate(subRow, PrintableRecord::elements)
        }
        grid.beforeGridRender.onSignal {params->
            result.add(params)
        }
        grid.render(record)
        assertEquals(2, result.size)
        result.output()
    }

    @Test
    fun `Grid correctly constructed when onResolved is used`(){
        var resolvedTriggered = 0
        val grid = buildPrettyGrid<PrintableRecord>(Grid.Grid1) {
            onResolved {
                resolvedTriggered ++
                buildRow(Row.SubTemplateRow){
                    add(component)
                }
            }
        }
        assertEquals(true, grid.singleLoader.valueResolved.signal)
        assertEquals(0, resolvedTriggered)
        val render = grid.render(record)
        render.output()
        assertEquals(1, resolvedTriggered)
        val row =  assertNotNull(grid.rows.firstOrNull())
        assertEquals(Row.SubTemplateRow, row.id)
        val cell = assertNotNull(row.cells.firstOrNull())
        assertIs<StaticCell>(cell)
        assertEquals(record.component, cell.text)
        assertEquals(1, grid.size, "Extra rows added")
        assertEquals(1, row.size, "Extra cells added")
        grid.render(record)
        assertEquals(1, resolvedTriggered)
    }
}
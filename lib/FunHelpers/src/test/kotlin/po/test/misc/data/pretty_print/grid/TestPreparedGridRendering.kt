package po.test.misc.data.pretty_print.grid

import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.common.Grid
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.prepareListGrid
import po.misc.data.pretty_print.prepareValueGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestPreparedGridRendering : PrettyTestBase(){


    private val record = createRecord()
    private val preSaved = prepareValueGrid(PrintableRecord::subClass){
        buildRow(Row.SubTemplateRow) {
            add(PrintableRecordSubClass::subName)
        }
    }
    private val preSavedList = prepareListGrid(PrintableRecord::elements, Grid.SubTemplateGrid){
        verbosity = Verbosity.Debug
        buildRow(Row.SubTemplateRow) {
            add(PrintableElement::elementName)
        }
    }
    private val staticText1 = "Static_1"

    @Test
    fun `Pre saved single value template rendering`(){
        val grid = buildGrid(Grid.Grid1){
           buildRow(Row.Row1) {
               add("Static")
           }
           useTemplate(preSaved)
            buildRow(Row.Row2) {
                add("Static2")
            }
        }
        val render = grid.render(record)
        val lines = render.lines()
        assertEquals(3, lines.size)
        assertTrue { lines[0].contains("Static") }
        assertTrue { lines[1].contains(record.subClass.subName) }
        assertTrue { lines[2].contains("Static2") }
    }
    @Test
    fun `Pre saved single value template build and render`(){
        val grid = buildGrid(Grid.Grid1){
            buildRow(Row.Row1) {
                add(staticText1)
            }
            useTemplate(preSaved){
                buildRow(Row.Row2){
                    add(PrintableRecordSubClass::subComponent)
                }
            }
            buildRow(Row.Row3) {
                add("Static2")
            }
        }
        val render = grid.render(record)
        val lines = render.lines()
        render.output()
        assertEquals(4, lines.size)
        assertTrue { lines[0].contains("Static") }
        assertTrue { lines[1].contains(record.subClass.subName) }
        assertTrue { lines[2].contains(record.subClass.subComponent) }
        assertTrue { lines[3].contains("Static2") }
    }
    @Test
    fun `Pre saved list template rendering`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(preSavedList)
        }
        val render = grid.render(record)
        val lines = render.lines()
        render.output(verbosity)
    }
}
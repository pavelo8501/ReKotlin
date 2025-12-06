package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertTrue

class TestGridRendering : PrettyTestBase(){

    private val headerText: String = "Header"

    private val subGrid = buildPrettyGrid(PrintableRecord::subClass){
        buildRow {
            addCell(PrintableRecordSubClass::subName)
        }
    }
    private val elementGrid = buildPrettyGrid<PrintableElement>{
        buildRow {
            addCell(PrintableElement::elementName)
        }
    }

    @Test
    fun `Grid renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText)
                addCell(PrintableRecord::name)
            }
        }
        val record = createRecord()
        val render =  grid.render(record)
        assertTrue { render.contains(headerText) && render.contains(record.name) }
    }

    @Test
    fun `Grid composition renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText)
            }
            useTemplate(subGrid, PrintableRecord::subClass)
        }
        val record = createRecord()
        val subName = record.subClass.subName
        val render =  grid.render(record)
        assertTrue { render.contains(headerText) }
        assertTrue { render.contains(subName) }
    }

    @Test
    fun `Grid composition with list property renders rows as expected`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText)
            }
            useListTemplate(elementGrid, PrintableRecord::elements)
        }
        val record = createRecord()
        val elementName = record.elements.first().elementName
        val render =  grid.render(record)
        render.output()
        assertTrue { render.contains(headerText) }
        assertTrue { render.contains(elementName) }

    }

}
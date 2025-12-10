package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.grid.addHeadedRow
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.grid.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.splitLines
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.stack_tracer.StackFrameMeta.Template
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridRendering : PrettyTestBase(){

    private val testGridRenderingString = "TestGridRendering"
    private val testGridInt = 10

    private val header = buildPrettyGrid<PrintableRecord>{
        addHeadedRow(templateHeaderText1)
        buildRow {
            addCell(PrintableRecord::name)
            addCell(PrintableRecord::description)
        }
    }

    private val elementRow = buildPrettyRow<PrintableElement>(RowPresets.Horizontal){
        addCell(PrintableElement::elementName)
    }

    private val valueGrid = buildPrettyGrid(PrintableRecord::subClass){
        buildRow {
            addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
    private val subGrid = buildPrettyGrid<PrintableRecordSubClass>{
        buildRow {
            addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }

    private val verticalSubGrid = buildPrettyGrid<PrintableRecordSubClass>(Orientation.Vertical){
        buildRow {
            addCells(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }

    private val record = createRecord()
    private val elementGrid = buildPrettyGrid<PrintableElement>{
        buildRow {
            addCell(templateHeaderText1)
        }
        buildRow {
            addCell(PrintableElement::elementName)
        }
    }
    private val thisGrid = buildPrettyGrid<TestGridRendering>{
        addHeadedRow(templateHeaderText1)
        buildRow {
            addCell(TestGridRendering::testGridRenderingString)
            addCell(TestGridRendering::testGridInt)
        }
    }

    @Test
    fun `Grid renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText1)
                addCell(PrintableRecord::name)
            }
        }
        val record = createRecord()
        val render =  grid.render(record)
        assertTrue { render.contains(headerText1) && render.contains(record.name) }
    }

    @Test
    fun `Grid composition grid + verticalSubGrid render`(){

        var builtGrid : PrettyValueGrid<* ,*>? = null
        val grid = buildPrettyGrid<PrintableRecord>{
            builtGrid = useTemplate(verticalSubGrid, PrintableRecord::subClass)
        }

        assertNotNull(builtGrid){valueGrid->
            assertEquals(Orientation.Vertical, valueGrid.options.orientation)
            assertEquals(verticalSubGrid.rows.size, valueGrid.rows.size)
            val row = verticalSubGrid.rows.first()
            val valueRow = valueGrid.rows.first()
            assertEquals(row.cells.size,  valueRow.cells.size)
        }

        val render = grid.render(record)
        render.output()
        val lineSplit = render.splitLines()
        assertEquals(2, lineSplit.size)

    }

    @Test
    fun `Grid composition renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText1)
            }
            useTemplate(subGrid, PrintableRecord::subClass)
        }
        val record = createRecord()
        val subName = record.subClass.subName
        val render =  grid.render(record)
        render.output()
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains(subName) }
    }

    @Test
    fun `Grid as list type template renders as expected`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            useListTemplate(elementGrid, PrintableRecord::elements)
        }
        val render = grid.render(record)
        render.output()
    }

    @Test
    fun `Grid composition with list property renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(headerText1)
            }
            useListTemplate(elementGrid, PrintableRecord::elements)
        }
        val record = createRecord()
        val elementName = record.elements.first().elementName
        val render =  grid.render(record)
        render.output()
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains(elementName) }
    }

    @Test
    fun `Multi part grid rendered as expected`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            useTemplate(header)
            useTemplate(elementRow, PrintableRecord::elements)
        }
        assertEquals(2, grid.rows.size)
        assertEquals(3, grid.renderBlocks.size)
        assertEquals(1, grid.rows.first().cells.size)
        assertEquals(2, grid.rows[1].cells.size)

        val elementGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(grid.renderBlocks.last())
        assertEquals(PrintableElement::class, elementGrid.type.kClass)
        assertEquals(PrintableRecord::class, elementGrid.hostType.kClass)

        val record = createRecord()
        val render =  grid.render(record)
        val lineSplit = render.splitLines()
        assertEquals(4, lineSplit.size)
        assertTrue { lineSplit.first().contains(headerText1) }
        assertTrue { lineSplit[1].contains("Name")  && lineSplit[1].contains(record.name) }
        assertTrue { lineSplit[1].contains("Description")  && lineSplit[1].contains(record.description) }
        assertTrue { lineSplit[2].contains("Element name")  && lineSplit[2].contains(record.elements[0].elementName) }
        assertTrue { lineSplit[3].contains("Element name")  && lineSplit[3].contains(record.elements[1].elementName) }
    }

    @Test
    fun `Foreign grid rendered as expected`(){

        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                addCell(PrintableRecord::name)
            }
            useTemplate(thisGrid){
                this@TestGridRendering
            }
        }
        val record = createRecord()
        grid.render(record)
    }
}
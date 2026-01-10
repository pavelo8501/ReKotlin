package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.functions.CallableKey
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridRendering : PrettyTestBase(){

    private val header = buildPrettyGrid<PrintableRecord>{
        buildRow{
            add(templateHeaderText1)
        }
        buildRow {
            add(PrintableRecord::name)
            add(PrintableRecord::description)
        }
    }
    private val elementRow = buildPrettyRow<PrintableElement>{
        orientation = Orientation.Horizontal
        add(PrintableElement::elementName)
    }
    private val subGrid = buildPrettyGrid<PrintableRecordSubClass>{
        buildRow {
            addAll(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
    private val verticalSubGrid = buildPrettyGrid<PrintableRecordSubClass>{
        orientation = Orientation.Vertical
        buildRow {
            addAll(PrintableRecordSubClass::subName, PrintableRecordSubClass::subComponent)
        }
    }
    private val record = createRecord()
    private val elementGrid = buildPrettyGrid<PrintableElement>{
        buildRow {
            add(templateHeaderText1)
        }
        buildRow {
            add(PrintableElement::elementName)
        }
    }

    val headerText1: String = "header_text_1"

    @Test
    fun `Grid renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(headerText1)
                add(PrintableRecord::name)
            }
        }
        val record = createRecord()
        val render =  grid.render(record)
        assertTrue { render.contains(headerText1) && render.contains(record.name) }
    }
    @Test
    fun `Grid composition grid + verticalSubGrid render`(){
        var builtGrid : PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>? = null
        val grid = buildPrettyGrid<PrintableRecord>{
            builtGrid = useGrid(verticalSubGrid, PrintableRecord::subClass)
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
        val lineSplit = render.lines()
        assertEquals(2, lineSplit.size)
    }
    @Test
    fun `Grid composition renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(headerText1)
            }
            useGrid(subGrid, PrintableRecord::subClass)
        }
        val record = createRecord()
        val subName = record.subClass.subName
        val render =  grid.render(record)
        assertTrue { render.contains(headerText1) }
        assertTrue { render.contains(subName) }
    }
    @Test
    fun `Grid composition with list property renders rows as expected`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(headerText1)
            }
            useGrid(elementGrid, PrintableRecord::elements)
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
            useGrid(header)
            useRow(elementRow, PrintableRecord::elements, Orientation.Vertical)
        }
        assertEquals(2, grid.rows.size)
        assertEquals(3, grid.renderPlan.renderNodes.size)
        assertEquals(1, grid.rows.first().cells.size)
        assertEquals(2, grid.rows[1].cells.size)
        assertNotNull(grid.renderPlan.renderNodes.firstOrNull()){ firstBlock->
            assertIs<PrettyRow<PrintableRecord>>(firstBlock)
            assertEquals(header.rows[0].size, firstBlock.size)
        }
        assertNotNull(grid.renderPlan.renderNodes.getOrNull(1)){ secondBlock->
            assertIs<PrettyRow<PrintableRecord>>(secondBlock)
            assertEquals(header.rows[1].size, secondBlock.size)
        }
        assertNotNull(grid.renderPlan.renderNodes.getOrNull(2)) { thirdBlock ->
            assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(thirdBlock)
            assertNotNull(thirdBlock.dataLoader[CallableKey.Property])
        }
        val elementGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(grid.renderPlan.renderNodes.last())
        assertEquals(PrintableElement::class, elementGrid.receiverType.kClass)
        assertEquals(PrintableRecord::class, elementGrid.sourceType.kClass)
        val render =  grid.render(record)
        val lineSplit = render.lines()
        assertTrue { lineSplit.first().contains(headerText1) }
        assertTrue { lineSplit[1].contains("Name")  && lineSplit[1].contains(record.name) }
        assertTrue { lineSplit[1].contains("Description")  && lineSplit[1].contains(record.description) }
        assertNotNull(lineSplit.getOrNull(2), "First line from elementRow is missing"){
            assertTrue { it.contains("Element name")  && it.contains(record.elements[0].elementName) }
        }
        assertNotNull(lineSplit.getOrNull(3), "Second line from elementRow is missing"){
            assertTrue { it.contains("Element name")  && it.contains(record.elements[1].elementName) }
        }
        assertEquals(4, lineSplit.size)
    }
}
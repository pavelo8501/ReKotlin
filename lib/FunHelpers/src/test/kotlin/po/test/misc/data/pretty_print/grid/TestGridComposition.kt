package po.test.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.buildPrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.buildGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.common.Grid
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.prepareListGrid
import po.misc.data.pretty_print.prepareValueGrid
import po.misc.data.pretty_print.parts.grid.RenderKey
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.prepareRow
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableElement
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridComposition : PrettyTestBase(){

    private val printableRecordSubClassGrid = buildPrettyGrid<PrintableRecordSubClass>{
        buildRow {
            add(PrintableRecordSubClass::subName)
        }
    }
    private val preSaved = prepareValueGrid(PrintableRecord::subClass){
        buildRow {
            add(PrintableRecordSubClass::subName)
        }
    }
    private val preSavedList = prepareListGrid(PrintableRecord::elements, Grid.SubTemplateGrid){
        buildRow(Row.SubTemplateRow) {
            add(PrintableElement::elementName)
        }
    }
    private val preSavedPrintableRecordSubClassRow = prepareRow(PrintableRecord::subClass, Row.SubTemplateRow){
        add(PrintableRecordSubClass::subName)
    }

    @Test
    fun `Inferred types`(){

        val grid = buildGrid<PrintableRecord> {
            buildRow{
                add(PrintableRecord::name)
            }
            buildRow(PrintableRecord::subClass){
                add(PrintableRecordSubClass::subName)
            }
            buildListGrid(PrintableRecord::elements){
                buildRow {
                    add(PrintableElement::elementName)
                }
            }
        }
        assertEquals(3, grid.renderPlan.renderSize)
        val rows = grid.renderPlan[PrettyRow]
        assertEquals(3, rows.size)
        assertEquals(PrintableRecord::class, rows[0].typeToken.kClass)
        assertEquals(PrintableRecordSubClass::class, rows[1].typeToken.kClass)
        assertEquals(PrintableElement::class, rows[2].typeToken.kClass)
    }

    @Test
    fun `Pre saved single value template usage`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(preSaved)
        }
        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()){
            assertEquals(1, it.rows.size)
        }
    }

    @Test
    fun `Pre saved single value template building usage`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(preSaved){
                buildRow(Row.Row1){
                    add(PrintableRecordSubClass::subName)
                    add("Static on Row1")
                }
                renderSourceHere()
                buildRow(Row.Row2){
                    add("Static on Row2")
                }
            }
        }
        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()){grid->
            assertEquals(3, grid.rows.size)
            grid.rows.output()
            grid.rows[0].templateID.output()
             assertEquals(Row.Row1, grid.rows[0].templateID)
             assertEquals(Row.Row2, grid.rows[2].templateID)
        }
    }

    @Test
    fun `Pre saved list  value template usage`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(preSavedList)
        }
        assertEquals(1, grid.renderPlan[PrettyRow].size)
        assertNotNull(grid.renderPlan[PrettyValueGrid].firstOrNull()){
            assertEquals(1, it.rows.size)
        }
    }

    @Test
    fun `Pre saved list value template building usage`(){
        val grid = buildPrettyGrid<PrintableRecord>{
            buildRow {
                add(PrintableRecord::name)
            }
            useTemplate(preSavedList){
                buildRow(Row.Row1){
                    add(PrintableElement::elementName)
                    add("Static on Row1")
                }
                renderSourceHere()
                buildRow(Row.Row2){
                    add("Static on Row2")
                }
            }
        }
       assertIs<PrettyRow<PrintableRecord>>(grid.renderPlan[RenderKey(0, RenderableType.ValueGrid)])
       val valueGrid =  assertIs<PrettyValueGrid<PrintableRecord, PrintableElement>>(
           grid.renderPlan[RenderKey(1, RenderableType.ValueGrid)]
       )
       assertNotNull(valueGrid.templateID){
           assertEquals(Grid.SubTemplateGrid, it)
       }
       assertEquals(3, valueGrid.rows.size)
       assertEquals(Row.Row1, valueGrid.rows[0].templateID)
       assertEquals(Row.SubTemplateRow, valueGrid.rows[1].templateID)
       assertEquals(Row.Row2, valueGrid.rows[2].templateID)
    }

    @Test
    fun `Grid accepts another Grid as template if transition property provided`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useGrid(printableRecordSubClassGrid, PrintableRecord::subClass)
        }
        assertNotNull(grid.renderPlan.renderNodes.firstOrNull()) { node ->
            val valueGrid = assertIs<PrettyValueGrid<PrintableRecord, PrintableRecordSubClass>>(node.element)
            assertEquals(PrintableRecordSubClass::class, valueGrid.receiverType.kClass)
            assertTrue(valueGrid.dataLoader.hasProperty)
            assertEquals(1, valueGrid.rows.size)
        }
    }

    @Test
    fun `Grid accepts  RowValueBuilder as template`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(preSavedPrintableRecordSubClassRow)
        }
        assertEquals(1, grid.renderPlan.size)
    }

    @Test
    fun `Grid accepts  RowValueBuilder  as template building ValueGrid`(){
        val grid = buildPrettyGrid<PrintableRecord> {
            useTemplate(preSavedPrintableRecordSubClassRow){
                renderSourceHere()
                buildRow {
                    add(PrintableRecordSubClass::subComponent)
                }
            }
        }
        assertNotNull(grid.renderPlan[PrettyValueRow].firstOrNull()){ valueRow->
            assertEquals(2, valueRow.size)
        }
    }
}
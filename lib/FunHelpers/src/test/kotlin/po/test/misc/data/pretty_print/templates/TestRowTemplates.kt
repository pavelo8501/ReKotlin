package po.test.misc.data.pretty_print.templates


import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.pretty_print.prepareValueGrid
import po.misc.types.token.TypeToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class TestRowTemplates {

    class SomeClass(val parameter: String = "Parameter")
    private  val dslEngine = DSLEngine()
    private val some = SomeClass()

    private val parameter = "Parameter"
    private val rowTemplate = buildPrettyRow<TestRowTemplates> {
        add(TestRowTemplates::parameter)
    }

    private val someRowTemplate = buildPrettyRow<SomeClass> {
        add(SomeClass::parameter)
    }

    @Test
    fun `Row template with same receiver type`(){
        val gridContainer = dslEngine.prepareGrid(TypeToken<TestRowTemplates>()){}
        val processedGrid = gridContainer.useRow(rowTemplate)
        val grid = gridContainer.finalizeGrid()
        assertEquals(1, grid.renderPlan.size)
        assertNotNull(grid.renderPlan.renderNodes.firstOrNull()){ renderable ->
            val row = assertIs<PrettyRow<TestRowTemplates>>(renderable.element)
            assertNotSame(rowTemplate, row)
            assertEquals(rowTemplate, row)
        }
    }

    @Test
    fun `Row template with access by property`(){
        val gridContainer = dslEngine.prepareGrid(TypeToken<TestRowTemplates>()){}
        gridContainer.useRow(someRowTemplate, TestRowTemplates::some)
        val grid = gridContainer.finalizeGrid()
        assertEquals(1, grid.renderPlan.size)
        assertNotNull(grid.renderPlan.renderNodes.firstOrNull()){ renderable->
            val convertedGrid = assertIs<PrettyValueGrid<TestRowTemplates, SomeClass>>(renderable.element)
            assertNotNull(convertedGrid.rows.firstOrNull()){row->
                assertEquals(SomeClass::class, row.receiverType.kClass)
                assertEquals(someRowTemplate, row)
            }
        }
    }
}
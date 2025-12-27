package po.test.misc.data.pretty_print.templates


import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.rows.buildPrettyRow
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
        val gridContainer = dslEngine.prepareGrid<TestRowTemplates>(TypeToken<TestRowTemplates>()){}
        val processedGrid = gridContainer.useTemplate(rowTemplate)
        val grid = gridContainer.finalizeGrid(null)
        assertEquals(1, grid.renderPlan.size)
        assertNotNull(grid.renderPlan.renderables.firstOrNull()){ renderable->
            val row = assertIs<PrettyRow<TestRowTemplates>>(renderable)
            assertNotSame(rowTemplate, row)
            assertEquals(rowTemplate, row)
        }
    }

    @Test
    fun `Row template with access by property`(){
        val gridContainer = dslEngine.prepareGrid<TestRowTemplates>(TypeToken<TestRowTemplates>()){}
        gridContainer.useTemplate(someRowTemplate, TestRowTemplates::some)
        val grid = gridContainer.finalizeGrid(null)
        assertEquals(1, grid.renderPlan.size)
        assertNotNull(grid.renderPlan.renderables.firstOrNull()){ renderable->
            val convertedGrid = assertIs<PrettyValueGrid<TestRowTemplates, SomeClass>>(renderable)
            assertNotNull(convertedGrid.rows.firstOrNull()){row->
                assertEquals(SomeClass::class, row.typeToken.kClass)
                assertEquals(someRowTemplate, row)
            }
        }
    }
}
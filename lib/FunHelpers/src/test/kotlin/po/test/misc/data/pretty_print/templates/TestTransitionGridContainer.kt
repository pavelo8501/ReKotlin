package po.test.misc.data.pretty_print.templates


import po.misc.collections.lambda_map.CallableDescriptor
import po.misc.data.pretty_print.TransitionGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.grid.buildPrettyGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTransitionGridContainer {

    class SomeClass(val parameter: String = "Parameter", )

    private  val dslEngine = DSLEngine()
    private val some = SomeClass()

    private val someClassGrid = buildPrettyGrid<SomeClass> {
        buildRow {
            add(SomeClass::parameter)
            add("Static")
        }
    }

    @Test
    fun `Transition grid test`(){
        val gridContainer = dslEngine.prepareGrid<TestTransitionGridContainer>(){}
        val transition =  gridContainer.useTemplate(someClassGrid, TestTransitionGridContainer::some)
        assertEquals(1, someClassGrid.rows.size)
        assertNotNull(transition.dataLoader[CallableDescriptor.CallableKey.ReadOnlyProperty])
        assertEquals(1, transition.rows.size)
    }

    @Test
    fun `Transition grid rendering`(){
        val gridContainer = dslEngine.prepareGrid<TestTransitionGridContainer>(){}
         gridContainer.useTemplate(someClassGrid, TestTransitionGridContainer::some)
         val grid = gridContainer.finalizeGrid(null)

        assertEquals(1, grid.renderPlan.renderables.size)
        assertNotNull(grid.renderPlan.renderables.firstOrNull()){
            assertIs<TransitionGrid<TestTransitionGridContainer, SomeClass>>(it)
        }
        val render =  grid.render(this)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue { lines[0].contains(some.parameter) }
    }

    @Test
    fun `Transition grid rendering with build`(){
        val gridContainer = dslEngine.prepareGrid<TestTransitionGridContainer>(){}
        gridContainer.useTemplate(someClassGrid, TestTransitionGridContainer::some){

        }
        val grid = gridContainer.finalizeGrid(null)
        assertEquals(1, grid.renderPlan.renderables.size)
        assertNotNull(grid.renderPlan.renderables.firstOrNull()){
            assertIs<TransitionGrid<TestTransitionGridContainer, SomeClass>>(it)
        }
        val render =  grid.render(this)
        val lines = render.lines()
        assertEquals(1, lines.size)
        assertTrue { lines[0].contains(some.parameter) }
    }


}
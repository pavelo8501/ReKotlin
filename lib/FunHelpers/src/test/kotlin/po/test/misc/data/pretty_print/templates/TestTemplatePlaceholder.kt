package po.test.misc.data.pretty_print.templates

import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.templates.PlaceholderLifecycle
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.styles.contains
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.reflect.full.isSubclassOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplatePlaceholder : PrettyTestBase() {

    private class ForeignClass(var parameter1: String = "Parameter_1")
    private val foreignTemplate = buildPrettyRow<ForeignClass>(Row.Row1) {
        add(ForeignClass::parameter1)
    }
    private val otherForeignTemplate = buildPrettyRow<ForeignClass>(Row.Row2) {
        add("Other")
    }

    private class OtherClass(var parameter: String = "OtherClass_parameter")
    private val otherTemplate = buildPrettyRow<OtherClass>(Row.Row1) {
        add(OtherClass::parameter)
    }

    private val record = createRecord()
    private val propertyColour = CellPresets.Property.style.colour

    @Test
    fun `Placeholders lifecycle SingleUse  modifier work as expected`() {

        val foreignClass = ForeignClass()
        val grid = buildGrid {
            createPlaceholder(ForeignClass::class, PlaceholderLifecycle.SingleUse)
        }
        var render =  grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[RenderableType.Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertNotNull(renderable.delegate)
            assertEquals(Row.Row1, renderable.delegate?.id)
            assertTrue { renderable.enabled }
            assertTrue { renderable.dataLoader?.canResolve?:false }
        }
        assertTrue { render.contains(foreignClass.parameter1) && render.contains(propertyColour) }
        foreignClass.parameter1 = "Another parameter text"
        render = grid.render(record) {
            renderWith(otherForeignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[RenderableType.Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertEquals(Row.Row1, renderable.delegate?.id)
        }
        assertFalse { render.contains("Other") }
        assertTrue {  render.contains(foreignClass.parameter1) }
    }

    @Test
    fun `Placeholders lifecycle Reusable  modifier work as expected`() {
        val foreignClass = ForeignClass()
        val grid = buildGrid {
            createPlaceholder(ForeignClass::class,  PlaceholderLifecycle.Reusable)
        }
        var render = grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[RenderableType.Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertNotNull(renderable.delegate)
            assertEquals(Row.Row1, renderable.delegate?.id)
            assertTrue { renderable.enabled }
            assertTrue { renderable.dataLoader?.canResolve?:false }
        }
        assertTrue { render.contains(foreignClass.parameter1)}
        foreignClass.parameter1 = "Another parameter text"
        render = grid.render(record) {
            renderWith(otherForeignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[RenderableType.Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertEquals(Row.Row2, renderable.delegate?.id)
        }
        assertFalse { render.contains(foreignClass.parameter1) }
        assertTrue {  render.contains("Other") }
    }

    @Test
    fun `Multiple placeholders are resolved to the correct templates`() {
        val foreignClass = ForeignClass()
        val otherClass = OtherClass()
        val grid = buildGrid {
            createPlaceholder(ForeignClass::class, PlaceholderLifecycle.Reusable)
            createPlaceholder(OtherClass::class,  PlaceholderLifecycle.Reusable)
        }
        val placeholders =  grid.renderPlan[RenderableType.Placeholder]
        assertEquals(2, placeholders.size)
        assertEquals(false, placeholders[0].enabled)
        assertEquals(false, placeholders[1].enabled)

        val render = grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
            renderWith(otherTemplate, otherClass)
        }
        val renderedLines = render.lines()
        assertEquals(2, renderedLines.size)
        assertEquals(true, placeholders[0].enabled)
        assertEquals(true, placeholders[1].enabled)
        assertTrue("Template order mismatch") { renderedLines[0].contains(foreignClass.parameter1) }
        assertTrue("Template order mismatch") { renderedLines[1].contains(otherClass.parameter) }
    }

    @Test
    fun `Placeholder lookup by companion`() {
        val grid = buildGrid {
            createPlaceholder(ForeignClass::class, PlaceholderLifecycle.Reusable)
            createPlaceholder(OtherClass::class,  PlaceholderLifecycle.Reusable)
        }
        val placeholders = grid.renderPlan[Placeholder::class]
        assertEquals(2, placeholders.size)
        assertTrue { placeholders[0]::class.isSubclassOf(Placeholder::class) }
    }
}
package po.test.misc.data.pretty_print.templates

import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.output.output
import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.templates.Lifecycle
import po.misc.data.strings.contains
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.reflect.full.isSubclassOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplatePlaceholder : PrettyTestBase() {

    private class ForeignClass(var parameter1: String = "Parameter_1")
    private val otherForeignTemplateText = "Other static text"
    private val foreignTemplate = buildPrettyRow<ForeignClass>(Row.Row1) {
        add(ForeignClass::parameter1)
    }
    private val otherForeignTemplate = buildPrettyRow<ForeignClass>(Row.Row2) {
        add(otherForeignTemplateText)
    }
    private class OtherClass(var parameter: String = "OtherClass_parameter")
    private val otherTemplate = buildPrettyRow<OtherClass>(Row.Row1) {
        add(OtherClass::parameter)
    }

    private val record = createRecord()
    private val propertyColour = CellPresets.Property.style.colour
    private val foreign = ForeignClass()

    @Test
    fun `Placeholders does not throw if no data source available during render`() {
        val staticText = "Static text"
        val grid = buildGrid {
            buildRow {
                add(PrintableRecord::name)
            }
            createPlaceholder<ForeignClass>()
            headedRow(staticText)
        }
        val render =  assertDoesNotThrow { grid.render(record) }
        render.output(enableOutput)
        assertNotNull(grid.renderPlan.getRenderable<TemplatePlaceholder<ForeignClass>, ForeignClass>())
        assertFalse("Render should not have ${foreign.parameter1}") { render.contains(foreign.parameter1) }
    }

    @Test
    fun `Placeholders  rendered if data source and template available`() {
        val staticText = "Static text"
        val grid = buildGrid {
            buildRow { add(PrintableRecord::name) }
            createPlaceholder<ForeignClass>()
            headedRow(staticText)
        }
        assertNotNull(grid.renderPlan.getRenderable<TemplatePlaceholder<ForeignClass>, ForeignClass>())
        val render =  assertDoesNotThrow {
            grid.render(record){
                renderWith(foreignTemplate, foreign)
            }
        }
        render.output(enableOutput)
        val lines = render.lines()
        assertEquals(3, lines.size)
        assertTrue("Built PrintableRecord row not rendered") { lines[0].contains(record.name)  }
        assertTrue("Template not rendered") { lines[1].contains(foreign.parameter1)  }
        assertTrue("PrintableRecord row staticText not rendered") { lines[2].contains(staticText)  }
    }


    @Test
    fun `Placeholders lifecycle SingleUse modifier work as expected`() {
        val foreignClass = ForeignClass()
        val grid = buildGrid {
            createPlaceholder<ForeignClass>(Lifecycle.SingleUse)
        }
        var render =  grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertNotNull(renderable.delegate)
            assertEquals(Row.Row1, renderable.delegate?.templateID)
            assertTrue { renderable.enabled }
            assertTrue { renderable.dataLoader.canResolve}
        }
        assertTrue { render.contains(foreignClass.parameter1) && render.contains(propertyColour) }
        foreignClass.parameter1 = "Another parameter text"
        render = grid.render(record) {
            renderWith(otherForeignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertEquals(Row.Row1, renderable.delegate?.templateID)
        }
        assertFalse { render.contains(otherForeignTemplateText) }
        assertTrue {  render.contains(foreignClass.parameter1) }
    }

    @Test
    fun `Placeholders lifecycle Reusable  modifier work as expected`() {
        val foreignClass = ForeignClass()
        val grid = buildGrid {
            createPlaceholder<ForeignClass>(Lifecycle.Reusable)
        }
        var render = grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
        }
        assertNotNull(grid.renderPlan[Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertNotNull(renderable.delegate)
            assertEquals(Row.Row1, renderable.delegate?.templateID)
            assertTrue { renderable.enabled }
            assertTrue { renderable.dataLoader.canResolve}
        }
        assertTrue { render.contains(foreignClass.parameter1)}
        foreignClass.parameter1 = "Another parameter text"
        render = grid.render(record) {
            renderWith(otherForeignTemplate, foreignClass)
        }
        render.output(enableOutput)
        assertNotNull(grid.renderPlan[RenderableType.Placeholder].firstOrNull()) { renderable ->
            assertIs<TemplatePlaceholder<ForeignClass>>(renderable)
            assertEquals(Row.Row2, renderable.delegate?.templateID)
        }
        assertTrue { render.contains(foreignClass.parameter1) }
        assertTrue {  render.contains(otherForeignTemplateText) }
    }

    @Test
    fun `Multiple placeholders are resolved to the correct templates`() {
        val foreignClass = ForeignClass()
        val otherClass = OtherClass()
        val grid = buildGrid {
            createPlaceholder<ForeignClass>()
            createPlaceholder<OtherClass>()
        }
        val placeholders =  grid.renderPlan[Placeholder]
        assertEquals(2, placeholders.size)
        assertEquals(false, placeholders[0].enabled)
        assertEquals(false, placeholders[1].enabled)

        val render = grid.render(record) {
            renderWith(foreignTemplate, foreignClass)
            renderWith(otherTemplate, otherClass)
        }
        render.output(enableOutput)
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
            createPlaceholder<ForeignClass>(Lifecycle.Reusable)
            createPlaceholder<OtherClass>(Lifecycle.Reusable)
        }
        val placeholders = grid.renderPlan[Placeholder]
        assertEquals(2, placeholders.size)
        assertTrue { placeholders[0]::class.isSubclassOf(Placeholder::class) }
    }
}
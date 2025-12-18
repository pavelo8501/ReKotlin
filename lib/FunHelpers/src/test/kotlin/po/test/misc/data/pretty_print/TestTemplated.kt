package po.test.misc.data.pretty_print

import po.misc.data.linesCount
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.buildRow
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTemplated : Templated<TestTemplated> {

    override val valueType: TypeToken<TestTemplated> = tokenOf()

    val stringProperty: String = "string"
    val intProperty: Int = 300

    @Test
    fun `Static type rendering`(){
        val row = buildRow {
            addCells(::intProperty, ::stringProperty)
        }
        assertEquals(2, row.size)
        assertNotNull(row.cells.first().row)
        val render = row.render()
        assertTrue { render.contains(stringProperty) }
        assertTrue { render.contains(intProperty.toString()) }
        assertEquals(1, render.linesCount)
    }

    @Test
    fun `Static type rendering with parameters`(){
        val row = buildRow {
            addCells(::intProperty, ::stringProperty)
        }
        val verticalOption = RowOptions(Orientation.Vertical)
        var render = row.render(verticalOption)
        assertTrue { render.contains(stringProperty) }
        assertTrue { render.contains(intProperty.toString()) }
        assertEquals(2, render.linesCount)

        val plainTextOption = RowOptions()
        plainTextOption.orientation = Orientation.Vertical
        plainTextOption.usePlain = true
        render = row.render(plainTextOption)
        assertFalse { render.contains(Colour.GreenBright.code) }
    }

    @Test
    fun `Static type rendering by rows render overload`(){
        val row = buildRow {
            addCells(::intProperty, ::stringProperty)
        }
        var render = row.renderPlain()
        assertTrue { render.contains(stringProperty) }
        assertTrue { render.contains(intProperty.toString()) }
        assertEquals(1, render.linesCount)

        render = row.renderPlain(Orientation.Vertical)
        assertFalse { render.contains(Colour.GreenBright.code) }
        assertFalse { render.contains(Colour.Magenta.code) }
        assertEquals(2, render.linesCount)
    }
}
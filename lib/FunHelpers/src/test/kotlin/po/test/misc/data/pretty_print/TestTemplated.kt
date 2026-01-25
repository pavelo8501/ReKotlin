package po.test.misc.data.pretty_print

import po.misc.data.linesCount
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestTemplated : Templated<TestTemplated> {

    override val receiverType: TypeToken<TestTemplated> = tokenOf()

    val stringProperty: String = "string"
    val intProperty: Int = 300

    @Test
    fun `Row rendering`(){
        val row = buildRow {
            addCells(::intProperty, ::stringProperty)
        }
        val render = row.render(this)
        assertEquals(2, row.size)
        assertTrue { render.contains(stringProperty) }
        assertTrue { render.contains(intProperty.toString()) }
        assertEquals(1, render.linesCount)
    }

    @Test
    fun `Row rendering with parameters`(){
        val row = buildRow {
            addCells(::intProperty, ::stringProperty)
        }
        val verticalOption = RowOptions(Orientation.Vertical)
        var render = row.render(this, verticalOption)

        assertTrue { render.contains(stringProperty) }
        assertTrue { render.contains(intProperty.toString()) }
        assertEquals(2, render.linesCount)

        val plainTextOption = RowOptions(Orientation.Horizontal)
        plainTextOption.orientation = Orientation.Vertical
        render = row.render(plainTextOption)
        assertFalse { render.contains(Colour.GreenBright.code) }
    }
}
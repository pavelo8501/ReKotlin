package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.styles.Colour
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.text.contains

class TestPrettyCell: Component {

    private val text1 = "Text_1"
    private val text2 = "Text_2"

    @Test
    fun `PrettyCell renderer work as expected`(){
        val cell = PrettyCell().colourConditions{
            Colour.Green.buildCondition {
                contains("Text_1")
            }
            Colour.Magenta.buildCondition {
                contains("Text_2")
            }
        }
        assertIs<PrettyCell>(cell)
        assertEquals(1, cell.textFormatter.formatters.size)
        var render = cell.render(text1)
        assertTrue { render.contains(text1) }
        assertTrue{ render.contains(Colour.Green.code) }

        render = cell.render(text2)
        assertTrue { render.contains(text2) }
        assertTrue{ render.contains(Colour.Magenta.code) }
    }

    @Test
    fun `PrettyCell renderer handles Any type same way as string`(){
        val cell = PrettyCell().colourConditions{
            Colour.Green.buildCondition {
                contains("Text_1")
            }
            Colour.Magenta.buildCondition {
                contains("Text_2")
            }
        }
        assertIs<PrettyCell>(cell)
        val textAsAny : Any = text2
        val render = cell.render(textAsAny)
        assertTrue { render.contains(text2) }
        assertTrue{ render.contains(Colour.Magenta.code) }
        render.output()
    }
}
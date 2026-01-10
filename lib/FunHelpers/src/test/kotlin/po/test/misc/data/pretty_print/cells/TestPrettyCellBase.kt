package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.styles.Colour
import kotlin.test.assertTrue

class TestPrettyCellBase {

    @Test
    fun `Colour modifications by builder function`() {
        val cell1Text = "Cell 1 text"
        val cell1Text2 = "Cell 2 text"
        val cell1 = StaticCell(cell1Text)
        cell1.colourConditions {
            add(Colour.Blue) { contains(cell1Text) }
            add(Colour.Cyan) { contains(cell1Text2) }
        }
        val rendered = cell1.render(cell1Text)
        rendered.output()
        assertTrue { rendered.contains(Colour.Blue.code) }
        val rendered2 = cell1.render(cell1Text2)
        rendered2.output()
        assertTrue { rendered2.contains(Colour.Cyan.code) }
    }

    @Test
    fun `Colour modifications as vararg parameter`() {

        val cell1Text = "Cell 1 text"
        val cell1Text2 = "Cell 2 text"
        val cell1 = StaticCell(cell1Text)

        cell1.colourConditions(
            ColourCondition(Colour.Blue){ contains(cell1Text) },
            ColourCondition(Colour.Cyan){ contains(cell1Text2) }
        )
        val rendered = cell1.render(cell1Text)
        rendered.output()
        assertTrue { rendered.contains(Colour.Blue.code) }
        val rendered2 = cell1.render(cell1Text2)
        rendered2.output()
        assertTrue { rendered2.contains(Colour.Cyan.code) }
    }

    @Test
    fun `Colour modifications by builder function and colour extension`() {

        val cell1Text = "Cell 1 text"
        val cell1Text2 = "Cell 2 text"
        val cell1 = StaticCell(cell1Text)

        cell1.colourConditions{
            Colour.Blue.buildCondition { contains(cell1Text) }
            Colour.Cyan.buildCondition { contains(cell1Text2) }
        }
        val rendered = cell1.render(cell1Text)
        rendered.output()
        assertTrue { rendered.contains(Colour.Blue.code) }
        val rendered2 = cell1.render(cell1Text2)
        rendered2.output()
        assertTrue { rendered2.contains(Colour.Cyan.code) }
    }

}
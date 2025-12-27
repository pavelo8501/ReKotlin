package po.test.misc.data.pretty_print.rows

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.Colour.RedBright
import po.misc.data.styles.ColourStyle
import po.misc.data.styles.colorize
import kotlin.test.Test

class TestRowSizing {

    private class FormattedClass(var text:String = "Some text", val useColour:Colour = RedBright): PrettyPrint{
        override val formattedString: String get() = text.colorize(useColour)
    }
    private class PlainClass(var text:String = "Other text", ){
        override fun toString(): String= text
    }

    private val formatted = FormattedClass()
    private val plain = PlainClass("Other text")

    @Test
    fun `Row cells are spread evenly`(){
        val row = buildPrettyRow<TestRowSizing>{
            add(TestRowSizing::plain)
            add(TestRowSizing::formatted)
            add("Static")
        }
        val render = row.render(this)
        render.output()
        val text = "Some Color Styled Text"

    }

}
package po.test.misc.data.pretty_print.rows

import po.misc.data.PrettyPrint
import po.misc.data.output.output
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.styles.Colour
import po.misc.data.styles.Colour.RedBright
import po.misc.data.styles.colorize
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRowSizing: PrettyTest<TestRowSizing>(true) {

    override val receiverType: TypeToken<TestRowSizing> = tokenOf()

    private class FormattedClass(var text:String = "Some text", val useColour:Colour = RedBright): PrettyPrint{
        override val formattedString: String get() = text.colorize(useColour)
    }
    private class PlainClass(var text:String = "Other text", ){
        override fun toString(): String= text
    }

    private val someText1 = "Text 1"
    private val someText2 = "Text 2"
    private val formatted = FormattedClass()
    private val plain = PlainClass("Other text")

    private fun projectedSize(vararg props: KProperty0<String>): Int{
        val  bordersCount: Int = props.size - 1
        var size = 0
        props.forEach {
            size += it.displayName.length + it.get().length
        }
        //Key value delimiter
        size += props.size
        //Border + surrounding whitespace
        size += (bordersCount * 3)
        return size
    }

    @Test
    fun `In compact row cells take min possible space`(){
        val row = buildRow{
            add(TestRowSizing::someText1)
            add(TestRowSizing::someText2)
            options.viewport = ViewPortSize.Console80
            options.layout = Layout.Compact
        }

        val projectedSize = projectedSize(::someText1, ::someText2)

        val render = row.render(this)
        val lines = render.lines()
        render.output(enableOutput)
        assertEquals(1, lines.size)
        assertEquals(projectedSize, render.lengthNoAnsi)
    }

    @Test
    fun `In stretched row cells are appended with whitespace to take max possible width`(){
        val row = buildRow{
            add(TestRowSizing::someText1)
            add(TestRowSizing::someText2)
            options.viewport = ViewPortSize.Console80
            options.layout = Layout.Stretch
        }
        val projectedSize = ViewPortSize.Console80.size
        val render = row.render(this)
        val lines = render.lines()
        render.output(enableOutput)
        assertEquals(1, lines.size)
        assertEquals(projectedSize, render.lengthNoAnsi)
    }

}
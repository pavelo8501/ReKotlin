package po.test.misc.data.pretty_print.rows

import po.misc.data.helpers.lengthNoAnsi
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.rows.underlined
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestRowRenderingExtensions: PrettyTest<TestRowRenderingExtensions>(){

    override val receiverType : TypeToken<TestRowRenderingExtensions> = tokenOf()

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"
    private val enableOutput = true

    @Test
    fun `Underline extension for rows type Unit`(){
        val staticCell1 = text1.toCell()
        val staticCell2 = text2.toCell()
        val row = PrettyRow(staticCell1, staticCell2)
        val render = row.underlined('*', Colour.RedBright)
        render.output(testVerbosity)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2) }
        assertTrue { lines[1].contains('*') }
    }

    @Test
    fun `Underline extension for typed rows `(){
        val staticCell1 = text1.toCell()
        val keyed = TestRowRenderingExtensions::text2.toCell()
        val row = PrettyRow(keyed, staticCell1)
        val render = row.underlined(this,  '*', Colour.RedBright)
        render.output(testVerbosity)
        val lines = render.lines()
        assertEquals(2, lines.size)
        assertTrue { lines[0].contains(text1) && lines[0].contains(text2) }
        assertTrue { lines[1].contains('*') }
    }
}
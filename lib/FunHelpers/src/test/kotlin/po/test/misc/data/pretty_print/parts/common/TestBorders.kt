package po.test.misc.data.pretty_print.parts.common

import po.misc.collections.asList
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.interfaces.named.Named
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBorders: PrettyTest<TestBorders>(){

    override val receiverType: TypeToken<TestBorders> =  tokenOf()

    private val defaultName:String = "Decorator"
    private val text: String = "Some text"
    private val textPair get() =  text.toPair()

    private val topSeparator get() = TaggedSeparator(BorderPosition.Top, "*")
    private val bottomSeparator get() = TaggedSeparator(BorderPosition.Bottom, "_")
    private val leftSeparator get() = TaggedSeparator(BorderPosition.Left, "|")
    private val rightSeparator get() = TaggedSeparator(BorderPosition.Right, "|")

    @Test
    fun `Border class initialization default`() {
        val borders = Decorator("Decorator")
        assertEquals(false, borders.enabled)
    }
    @Test
    fun `Border class initialization applied separator`() {
        val borders = Decorator("Decorator").addSeparator(bottomSeparator)
        assertEquals(true, borders.enabled, "borders disabled")
        assertEquals(true, borders[BorderPosition.Bottom].enabled, "Bottom border not enabled")
    }
    @Test
    fun `Text wrap bottom border`() {
        val borders = Decorator(defaultName).addSeparator(bottomSeparator)
        val wrapped = borders.decorate(text.toPair(), Decorator.Metrics(text.length, 1))
        wrapped.output()
        val lines = wrapped.lines
        assertEquals(2, lines.size)
    }
    @Test
    fun `Text wrap top and bottom borders`() {
        val borders = Decorator(defaultName, topSeparator, bottomSeparator)
        val pair = text.toPair()
        val wrapped = borders.decorate(pair, Decorator.Metrics(pair.plainLength))
        wrapped.output()
        val lines = wrapped.lines
        assertEquals(3, lines.size)
        assertTrue { lines[0].contains("*****") }
        assertEquals(text, lines[1].plain)
        assertTrue { lines[2].contains("____") }
    }
    @Test
    fun `Text wrap top, bottom and left borders`() {
        val borders = Decorator(defaultName,  topSeparator, bottomSeparator, leftSeparator)
        val pair = text.toPair()
        val wrapped = borders.decorate(pair, Decorator.Metrics(pair.plainLength))
        wrapped.output()
        val lines = wrapped.lines
        assertEquals(3, lines.size)
        assertTrue { lines[0].contains("*****") }
        assertTrue { lines[1].contains(text) && lines[1].contains("|") }
        assertTrue { lines[2].contains("____")  }
    }
    @Test
    fun `Text wrap all borders`() {
        val rightSeparator = TaggedSeparator(BorderPosition.Right, "?")
        val borders = Decorator(defaultName, topSeparator, bottomSeparator, leftSeparator, rightSeparator)
        val wrapped = borders.decorate(textPair, Decorator.Metrics(textPair))
        wrapped.output()
        val lines = wrapped.lines
        assertEquals(3, lines.size)
        assertTrue { lines[0].contains("*****") && lines[0].contains("|") && lines[0].contains("?") }
        assertTrue { lines[1].contains(text) && lines[1].contains("|") && lines[1].contains("?") }
        assertTrue { lines[2].contains("____") && lines[2].contains("|") && lines[2].contains("?") }
    }
    @Test
    fun `Tagged separators are distributed correctly despite order mismatch`() {
        val name = "TestTagged"
        val borders = Decorator(name, rightSeparator, bottomSeparator, leftSeparator, topSeparator)
        assertEquals<ExtendedString>(borders[BorderPosition.Top], topSeparator)
        assertEquals<ExtendedString>(borders[BorderPosition.Bottom], bottomSeparator)
    }
}
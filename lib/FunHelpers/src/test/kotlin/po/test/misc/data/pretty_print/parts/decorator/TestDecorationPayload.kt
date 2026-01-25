package po.test.misc.data.pretty_print.parts.decorator

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.decorator.DecorationPolicy
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.interfaces.named.Named
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test

class TestDecorationPayload : PrettyTest<TestDecorationPayload>(true), Named {

    override val receiverType: TypeToken<TestDecorationPayload> get() = tokenOf()

    private val topSeparator get() = TaggedSeparator(BorderPosition.Top, "*")
    private val bottomSeparator get() = TaggedSeparator(BorderPosition.Bottom, "_")
    private val leftSeparator get() = TaggedSeparator(BorderPosition.Left, "|")
    private val rightSeparator get() = TaggedSeparator(BorderPosition.Right, "|")

    private val shortText = "Text 1"
    private val bitLongerText = "Text 2 bit longer"
    private val longerText = "Text 3 longer than text 2"

    @Test
    fun `Decorator policy MandatoryGap enforces presence of vertical gap`(){

        val decorator = Decorator{

            addSeparator(topSeparator, bottomSeparator, leftSeparator, rightSeparator)
        }
        val line1 = shortText.toPair()
        val line2 = bitLongerText.toPair()
        val line3 = longerText.toPair()

        val metrics = Decorator.Metrics(20, 40, leftOffset = 0)
        val render =   decorator.decorate(listOf(line1, line2, line3), metrics)
        render.output()

    }

}
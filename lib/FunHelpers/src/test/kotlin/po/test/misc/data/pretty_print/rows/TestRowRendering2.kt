package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test

class TestRowRendering2 : PrettyTest<TestRowRendering2>() {

    override val receiverType: TypeToken<TestRowRendering2> = tokenOf()

    val headerText1: String = "header_text_1"
    val auxText: String = "Aux_Text"

    private val text1 = "Text 1"
    private val text2 = "Text 2 bit longer"

    @Test
    fun `keyed cells`(){
        val cell1 = ::text1.toCell()
        val cell2 = ::text2.toCell()
        val row = PrettyRow(cell1, cell2)
        row.render(this).output(testVerbosity)
    }
}
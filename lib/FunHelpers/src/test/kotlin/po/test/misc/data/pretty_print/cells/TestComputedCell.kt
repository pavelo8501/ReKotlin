package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.buildPrettyRow
import kotlin.test.assertTrue

class TestComputedCell {

    private class ReportSubclass(val parameter: String = "Report subclass parameter")

    private class ReportClass (
        private val subParameterText: String = "Report subclass parameter",
        val text: String = "Report class text",
        val result: PrettyCellResult = PrettyCellResult.AccessPretty,
    ){
        enum class PrettyCellResult { AccessPretty, ReadPretty }
        val subParameter: ReportSubclass = ReportSubclass(subParameterText)
    }

    @Test
    fun `ComputedCell rendering as expected`(){

        val prettyRow = buildPrettyRow<ReportClass> {
            addCell(ReportClass::subParameter){
                it.parameter
            }
        }
        val subClassText = "Report subclass parameter"
        val report = ReportClass(subClassText)
        val renderedText =  prettyRow.render(report, PrettyRow.Orientation.Horizontal)
        renderedText.output()
        assertTrue { renderedText.contains(subClassText) }


    }

}
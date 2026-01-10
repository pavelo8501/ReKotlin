package po.test.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.styles.Colour
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.String
import kotlin.test.Test

class TestComputedRowRender : PrettyTestBase() {

    private class Record(val message: String, val recordType: Type){
        enum class Type { Info, Success, Failure, Warning }
    }

    private val record = Record("message", Record.Type.Info)

    @Test
    fun `Key and values rendered correctly`() {
        val template: PrettyRow<Record> = buildPrettyRow {
            add(Record::recordType) {
                colourConditions {
                    Colour.Blue.buildCondition { contains(Record.Type.Info.name) }
                    Colour.Green.buildCondition { contains(Record.Type.Success.name) }
                    Colour.Red.buildCondition { contains(Record.Type.Failure.name) }
                    Colour.YellowBright.buildCondition { contains(Record.Type.Warning.name) }
                }
            }
            addKeyless(Record::message)
        }
        val render = template.render(record)
        render.output()
    }
}
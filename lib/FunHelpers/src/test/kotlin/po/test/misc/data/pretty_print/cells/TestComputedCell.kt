package po.test.misc.data.pretty_print.cells


import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.buildPrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.data.styles.contains
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import po.test.misc.data.pretty_print.setup.PrintableRecordSubClass
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.text.contains

class TestComputedCell : PrettyTestBase(), TextStyler {

    private var outputEnabled:Boolean = true

    private var string = "Some string"

    fun giveString(source:TestComputedCell):String {
        return source.string
    }

    @Test
    fun `By default toString method of an instance is rendered`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(PrintableRecord::subClass){subClass->
                subClass
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        val record = createRecord()
        val subClassToString = record.subClass.toString()
        val render = cell.render(record)
        render.output(outputEnabled)
        assertTrue { render.contains(subClassToString) }
        assertTrue { render.contains(ComputedCell.keyless.style.colour) }
    }

    @Test
    fun `Computed Cell respects useSourceFormatting modifier respectively printing formattedString of a class`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(PrintableRecord::subClass){ subClass->
                cellOptions.sourceFormat = true
                subClass
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        val record = createRecord()
        val subClassFormattedString = record.subClass.formattedString
        val render = cell.render(record)
        render.output(outputEnabled)
        assertTrue { render.contains(subClassFormattedString) }
        assertTrue { render.contains(Colour.Blue.code) }
    }

    @Test
    fun `Computed Cell passing itself to render would not lead to overflow`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(PrintableRecord::subClass){
                this@add
            }
        }
        val cell = assertIs<ComputedCell<PrintableRecord, PrintableRecordSubClass>>(row.cells.first())
        assertDoesNotThrow {
            cell.render(createRecord())
        }
    }

    @Test
    fun `Computed cell conditional colour builder works as expected`(){
        val cell = ComputedCell(TestComputedCell::string){parameter->
            Colour.Magenta.buildCondition {
                parameter ==  "Some string"
            }
            Colour.Green.buildCondition {
                parameter ==  "Other string"
            }
            parameter
        }
        var render = cell.render(this)
        render.output(outputEnabled)
        assertTrue { render.contains(Colour.Magenta) }
        string = "Other string"
        render = cell.render(this)
        render.output(outputEnabled)
        assertTrue { render.contains(string) }
        assertTrue { render.contains(Colour.Green) }
    }


    @Test
    fun `Computed not from property`(){

        val cell = ComputedCell(::giveString){

        }

    }

}
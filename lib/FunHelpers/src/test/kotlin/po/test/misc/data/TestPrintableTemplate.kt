package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.asContext
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.Template
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.styles.text
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import kotlin.test.assertEquals

class TestPrintableTemplate: CTX {

    override val identity = asContext()

    data class PrintableRecord (
        override val producer: CTX,
        val personalName: String = "personalName",
        val componentName: String = "Some name",
        val description : String = "description",
        val intValue: Int = 200
    ): PrintableBase<PrintableRecord>(Printable){

        override val self: PrintableRecord = this

        companion object: PrintableCompanion<PrintableRecord>({PrintableRecord::class}){

            val Printable : Template<PrintableRecord> = createTemplate{
                next{
                    personalName
                }
            }
        }
    }

    @Test
    fun `DSL Builder saves lambda for deferred calculation`(){

            val record1 = PrintableRecord(this, "personalName")
            record1.echo()
            assertEquals(PrintableRecord.Printable,  record1.defaultTemplate, "Template was not persisted")
            assertEquals("personalName", record1.formattedString)
    }


    fun `Text colorization`(){

        val unstyled: String = "Simple text"
        val colorized = unstyled.colorize(Colour.CYAN)
        val colorized2 = Colour.BLUE text unstyled

        println(colorized)
        println(colorized2)
    }


    fun `Text conditional print`(){

        val text1: String = "Simple text 1"
        val text2: String = "Simple text 2"

        var result : Int = 12

        val resultingText = matchTemplate(
            templateRule(text1){
                result == 10
            },
            templateRule(text2){
                result >= 11
            }
        )

        assertEquals(text2, resultingText, "Wrong template selected")
    }
}
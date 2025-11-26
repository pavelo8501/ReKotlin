package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.styles.text
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.functions.dsl.helpers.nextBlock
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals

class TestPrintableTemplate {


    class NEWClas(
        val name: String = "Stroka"
    )

    data class PrintableRecord (
        val personalName: String = "personalName",
        val componentName: String = "Some name",
        val description : String = "description",
        val newClass: NEWClas = NEWClas(),
        val intValue: Int = 200
    ): PrintableBase<PrintableRecord>(this){

        override val self: PrintableRecord = this

        companion object: PrintableCompanion<PrintableRecord>(TypeToken.create()){

            val Printable : Template<PrintableRecord> = createTemplate{
                nextLine {
                    personalName
                }
                nextLine {
                    "ssflsdkfjlof"
                }
                nextBlock({ it.newClass }){
                    name
                }
            }
        }
    }

    @Test
    fun `DSL Builder saves lambda for deferred calculation`(){

            val record1 = PrintableRecord("personalName")
            record1.echo()
            assertEquals(PrintableRecord.Printable,  record1.activeTemplate, "Template was not persisted")
            assertEquals("personalName", record1.formattedString)
    }


    fun `Text colorization`(){

        val unstyled: String = "Simple text"
        val colorized = unstyled.colorize(Colour.Cyan)
        val colorized2 = Colour.Blue text unstyled

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
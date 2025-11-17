package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.printableProxy
import po.misc.data.processors.DataProcessor
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.context.asIdentity
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.nextLine
import po.misc.types.token.TypeToken

import po.test.misc.data.TestLogProxy.ArbitraryData.Companion.Template2
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestLogProxy: CTX {


    override val identity: CTXIdentity<out CTX> = asIdentity()


    data class TopDataItem (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<TopDataItem>(this){


        override val self: TopDataItem = this


        companion object: PrintableCompanion<TopDataItem>(TypeToken.create()){

            val TopTemplate = createTemplate {
                nextLine {
                    "TopTemplate->$content"
                }
            }
        }
    }

    data class ArbitraryData (
        val personalName: String,
        val componentName: String = "Some name",
        val message: String
    ): PrintableBase<ArbitraryData>(this){

        override val self: ArbitraryData = this


        companion object: PrintableCompanion<ArbitraryData>(TypeToken.create()){

            val Template2 =createTemplate {
                nextLine {
                    "Template2-> $personalName | Message ->  ${message.colorize(Colour.Red)}"
                }
            }
        }
    }

    val dataProcessor: DataProcessor<TopDataItem> = DataProcessor(null)

    @Test
    fun `Log proxy forward data to processor`(){

        val warning = printableProxy(this, Template2){
            val data = ArbitraryData(contextName, "TestLogProxy", it.message)

            data.setDefaultTemplate(Template2)
            dataProcessor.log(data)
        }

        warning.logMessage("test str")
        var received  : PrintableBase<*>? = null
        dataProcessor.hooks.onArbitraryDataReceived {
            it.echo()
            received  = it
        }
        warning.logMessage("test str")
        val receivedData = assertNotNull(received)
        assertTrue(receivedData.formattedString.contains("test str"), "formattedString does not contain message part")
    }

}
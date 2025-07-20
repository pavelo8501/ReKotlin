package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.printableProxy
import po.misc.data.processors.DataProcessor
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.context.asContext

import po.test.misc.data.TestLogProxy.ArbitraryData.Companion.Template2
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestLogProxy: CTX {

    override val identity = asContext()

    override val contextName: String
        get() = "TestLogProxy"

    data class TopDataItem (
        override val producer: CTX,
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<TopDataItem>(TopTemplate){
        override val self: TopDataItem = this


        companion object{
            val TopTemplate: PrintableTemplate<TopDataItem> = PrintableTemplate(){"TopTemplate->$content"}
        }
    }

    data class ArbitraryData (
        override val producer: CTX,
        val personalName: String,
        val componentName: String = "Some name",
        val message: String
    ): PrintableBase<ArbitraryData>(Template2){

        override val self: ArbitraryData = this


        companion object{
            val Template2: PrintableTemplate<ArbitraryData> = PrintableTemplate<ArbitraryData>(){
                "Template2-> $personalName | Message ->  ${message.colorize(Colour.RED)}"
            }
        }
    }

    val dataProcessor: DataProcessor<TopDataItem> = DataProcessor(null)

    @Test
    fun `Log proxy forward data to processor`(){
        val warning = printableProxy(this, Template2){
            val data = ArbitraryData(this, contextName, "TestLogProxy", it.message)
            dataProcessor.logData(data, data.defaultTemplate)
        }

        warning.logMessage("test str")
        var received  : PrintableBase<*>? = null
        dataProcessor.hooks.arbitraryDataReceived {
            it.echo()
            received  = it
        }
        warning.logMessage("test str")
        val receivedData = assertNotNull(received)
        assertTrue(receivedData.formattedString.contains("test str"), "formattedString does not contain message part")
    }

}
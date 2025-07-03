package po.test.lognotify.debug

import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.debug.debugProxy
import po.lognotify.debug.extensions.captureProperty
import po.lognotify.debug.models.CaptureBlock
import po.lognotify.extensions.runTask
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestDebugProxy: TasksManaged {

    data class DataItem(
        override val emitter: IdentifiableContext,
        val content: String,
    ): PrintableBase<DataItem>(Debug) {
        override val self: DataItem = this
        companion object : PrintableCompanion<DataItem>({ DataItem::class }) {
            val Debug: PrintableTemplate<DataItem> = PrintableTemplate("Debug"){
                "Name:${emitter.contextName} Content:$content"
            }
        }
    }

    override val contextName: String = "TestDebugProxy"

    init {
        val handler = logNotify()
        handler.notifierConfig {
            allowDebug(DataItem)
        }
    }
    val debugger = debugProxy(this, DataItem){
        DataItem(this, it.message)
    }


    @Test
    fun `Debug proxy is able to log method input parameters of task launchers`(){

        fun method1(param1: String) = runTask("method1", debugProxy = debugger.captureInput(param1)){}
        fun method2(param1: String, listParameter: List<Int>) =
            runTask("method2", debugProxy = debugger.captureInput(param1, listParameter)){}

        method1("Input")

        val inputParameters = debugger.inputParameters()
        assertEquals(1, inputParameters.size)

        val parameter = assertNotNull(inputParameters.firstOrNull(), "No input parameters captured")
        assertEquals("String", parameter.typeData.simpleName, "Wrong parameter simple name")
        assertEquals("Input", parameter.value, "Wrong parameter value")

        val inputList = listOf(1,2,3)
        method2("Input", inputList)

        val lastParameter = assertNotNull(inputParameters.lastOrNull(), "No input parameters captured")
        assertEquals("List", lastParameter.typeData.simpleName, "Wrong parameter simple name")
        assertEquals(3, lastParameter.size, "Size wrong")
        val firstItem = lastParameter.items.first()
        val lastItem = lastParameter.items.last()
        assertEquals("1", firstItem.value, "Wrong value of last item")
        assertEquals("3", lastItem.value, "Wrong value of last item")
    }

    @Test
    fun `Debug proxy is able to log safely complex type method input parameter(with type)`(){

        data class InputData(val id: Long = 10, val other: String = "Some str")

        fun method1(param1: InputData) =
            runTask("method1", debugProxy = debugger.capture(param1){
                captureProperty(parameter.id)
                captureProperty(parameter.other)
            }){

            }
        val inputData = InputData()
        method1(inputData)

        val inputParameters = debugger.inputParameters()
        val parameter = assertNotNull(inputParameters.firstOrNull(), "No input parameters captured")
        assertEquals("InputData", parameter.typeData.simpleName, "Wrong parameter simple name")
        assertEquals(2, parameter.items.size, "Size wrong")
        val id = parameter.items.first()
        val other = parameter.items.last()
        assertEquals(inputData.id.toString(), id.value, "Wrong value of last item")
        assertEquals(inputData.other, other.value, "Wrong value of last item")
    }
}
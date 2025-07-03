package po.test.lognotify.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.lognotify.TasksManaged
import po.lognotify.debug.debugProxy
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext

class TestDebugProxy: TasksManaged {

    override val contextName: String = "TestDebugProxy"

    data class DataItem(
        override val emitter: IdentifiableContext,
        val content: String,
    ): PrintableBase<DataItem>(Debug) {
        override val self: DataItem = this

        companion object : PrintableCompanion<DataItem>({ DataItem::class }) {
            val Debug: PrintableTemplate<DataItem> = PrintableTemplate("Debug"){
                "Debug[Name:$emitter Content:$content"
            }
        }
    }

    val proxy = debugProxy(this, DataItem){
        DataItem(this, it.message)
    }


    @Test
    fun `Debug proxy overall usability`() {
        assertDoesNotThrow {
            proxy.debug("Message")
        }
    }

}
package po.test.lognotify.debug

import po.lognotify.TasksManaged
import po.lognotify.interfaces.FakeTasksManaged
import po.misc.context.CTX
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion


class TestDebugProxy: FakeTasksManaged {

    data class DataItem(
        override val producer: CTX,
        val content: String,
    ): PrintableBase<DataItem>(Debug) {
        override val self: DataItem = this
        companion object : PrintableCompanion<DataItem>({ DataItem::class }) {
            val Debug: PrintableTemplate<DataItem> = PrintableTemplate(){
                "Name:${producer.contextName} Content:$content"
            }
        }
    }

    override val contextName: String = "TestDebugProxy"

    init {
       logHandler.notifierConfig {
            allowDebug(DataItem)
        }
    }


    fun `Debug proxy is able to log safely complex type method input parameter(with type)`(){

    }
}
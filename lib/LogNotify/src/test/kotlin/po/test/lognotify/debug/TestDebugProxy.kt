package po.test.lognotify.debug

import po.lognotify.TasksManaged
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.IdentifiableClass

class TestDebugProxy: TasksManaged {

    data class DataItem(
        override val producer: IdentifiableClass,
        val content: String,
    ): PrintableBase<DataItem>(Debug) {
        override val self: DataItem = this
        companion object : PrintableCompanion<DataItem>({ DataItem::class }) {
            val Debug: PrintableTemplate<DataItem> = PrintableTemplate("Debug"){
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
package po.test.lognotify.debug

import po.lognotify.interfaces.FakeTasksManaged
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine


class TestDebugProxy: FakeTasksManaged {

    data class DataItem(
        val producer: CTX,
        val content: String,
    ): PrintableBase<DataItem>(this) {
        override val self: DataItem = this
        companion object : PrintableCompanion<DataItem>({ DataItem::class }) {

            val Debug: Template<TestDebugProxy.DataItem>  = createTemplate {
                nextLine{
                    "Name:${producer.contextName} Content:$content"
                }
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
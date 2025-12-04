package po.test.lognotify.debug

import po.test.lognotify.setup.FakeTasksManaged
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.types.token.TypeToken


class TestDebugProxy: FakeTasksManaged {

    data class DataItem(
        val producer: CTX,
        val content: String,
    ): PrintableBase<DataItem>(this) {
        override val self: DataItem = this
        companion object : PrintableCompanion<DataItem>(TypeToken.create()) {

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

}
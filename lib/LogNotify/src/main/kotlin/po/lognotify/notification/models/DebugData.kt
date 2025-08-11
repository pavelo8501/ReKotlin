package po.lognotify.notification.models

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.debugging.DebugTopic
import po.misc.debugging.toConsoleLink
import po.misc.exceptions.models.StackFrameMeta
import po.misc.functions.dsl.helpers.nextBlock


class DebugData(
   val message: String,
   val contextName: String,
   val completeContextName: String,
   val stackMeta: List<StackFrameMeta>

): PrintableBase<DebugData>(this) {

    var debugTopic: DebugTopic = DebugTopic.General
        private set

    fun setTopic(topic: DebugTopic): DebugData {
        debugTopic = topic
        return this
    }
    override val self: DebugData = this

    companion object : PrintableCompanion<DebugData>({ DebugData::class }) {
        val Default: Template<DebugData> = createTemplate {
            nextBlock {
                "[DEBUG][${debugTopic.name}] in $contextName -> $message"
            }
        }
        val Extended: Template<DebugData> = createTemplate {
            nextLine{
                "[DEBUG][${debugTopic.name}] in $contextName, Calling Method:${stackMeta[0].methodName}"
            }
            nextLine {
                "Call site: ${stackMeta[0].toConsoleLink()}"
            }
            nextBlock {
                "-> $message"
            }
        }
    }
}
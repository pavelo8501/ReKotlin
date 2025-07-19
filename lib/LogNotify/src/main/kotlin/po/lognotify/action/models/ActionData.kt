package po.lognotify.action.models

import po.lognotify.action.ActionSpan
import po.lognotify.classes.notification.models.LogData
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.context.IdentifiableClass

class ActionData(
    val actionSpan: ActionSpan<*, *>,
    val actionName: String,
    val status: ActionSpan.Status,
    val propertySnapshot: List<PropertyData> = emptyList()
): PrintableBase<ActionData>(Info) {

    override val self: ActionData = this

    override val producer : IdentifiableClass get() = actionSpan

    companion object : PrintableCompanion<ActionData>({ ActionData::class }) {

        val nestingFormatter: LogData.() -> String = {
            matchTemplate(
                templateRule(nestingLevel.toString()) { nestingLevel > 0 },
                templateRule("Root ".colorize(Colour.GREEN)) { nestingLevel == 0 }
            )
        }

        val Info = PrintableTemplate<ActionData>("Info") {
            "ActionSpan[${actionName}] Running in context[${actionSpan.receiver.contextName}] On Task[${actionSpan.taskBase.key.taskName}]" +
                    "Status[${
                        matchTemplate(
                            templateRule(actionSpan.status.name.colorize(Colour.BRIGHT_WHITE)) { actionSpan.status == ActionSpan.Status.Active },
                            templateRule(actionSpan.status.name.colorize(Colour.GREEN)) { actionSpan.status == ActionSpan.Status.Complete },
                            templateRule(actionSpan.status.name.colorize(Colour.RED)) { actionSpan.status == ActionSpan.Status.Failed },
                        )
                    }]"
        }

        val Backtrace: PrintableTemplate<ActionData> = PrintableTemplate("Backtrace") {
            "${actionName}[Status: ${status.name}; in context ${producer.contextName} ] ${
                propertySnapshot.joinToString(separator = SpecialChars.NewLine.char) {
                    it.formattedString
                }
            }"
        }
    }
}
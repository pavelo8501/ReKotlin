package po.lognotify.classes.notification.models

import po.lognotify.action.ActionSpan
import po.lognotify.tasks.ExecutionStatus
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.Template
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule

class ActionData(
    val actionSpan: ActionSpan<*, *>,
    val actionName: String,
    val status: ExecutionStatus,
    val propertySnapshot: List<PropertyData> = emptyList()
): PrintableBase<ActionData>(Info) {

    override val self: ActionData = this

    val producer: CTX get() = actionSpan

    companion object : PrintableCompanion<ActionData>({ ActionData::class }) {

        val Info: Template<ActionData> = createTemplate(""){
            next {
                "$actionName Scope[${producer.completeName} Status["
            }
            next {
                "${matchTemplate(
                    templateRule(status.name.colorize(Colour.BRIGHT_WHITE)) { status == ExecutionStatus.Active },
                    templateRule(status.name.colorize(Colour.GREEN)) { status == ExecutionStatus.Complete },
                    templateRule(status.name.colorize(Colour.RED)) { status == ExecutionStatus.Failing },
                    templateRule(status.name.colorize(Colour.RED)) { status == ExecutionStatus.Faulty }
                )}]"
            }
        }
    }
}
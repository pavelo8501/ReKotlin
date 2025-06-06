package po.lognotify.classes.notification.models

import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.models.TaskKey
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.console.helpers.emptyOnNull
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import po.misc.time.ExecutionTimeStamp

data class TaskData(
    val taskKey: TaskKey,
    val config: TaskConfig,
    val timeStamp : ExecutionTimeStamp,
    val message: String,
    val severity: SeverityLevel,
): PrintableBase<TaskData>(){

    override val self: TaskData = this

    override val itemId: ValueBased= toValueBased(taskKey.taskId)
    override val emitter : Identifiable = asIdentifiable(taskKey.taskName, taskKey.moduleName)

    companion object {

        val nestingFormatter: TaskData.() -> String = {
            matchTemplate(
                templateRule(taskKey.nestingLevel.toString()) { taskKey.nestingLevel > 0 },
                templateRule("Root ".colorize(Colour.GREEN)) { taskKey.nestingLevel == 0 }
            )
        }

        val prefix: TaskData.(auxMessage: String) -> String = { auxMessage ->
            "${Colour.makeOfColour(Colour.BLUE, "[${auxMessage}")}  ${nestingFormatter(this)}" +
                    "${taskKey.taskName} | ${taskKey.moduleName} @ $currentTime".colorize(Colour.BLUE)
        }

        val messageFormatter: TaskData.() -> String = {
            matchTemplate(
                templateRule(message) { severity == SeverityLevel.INFO },
                templateRule(message.colorize(Colour.YELLOW)) { severity == SeverityLevel.WARNING },
                templateRule(message.colorize(Colour.RED)) { severity == SeverityLevel.EXCEPTION }
            )
        }

        val Header: PrintableTemplate<TaskData> = PrintableTemplate {
            prefix.invoke(this, "Start") +
                    "${config.actor.emptyOnNull("by ")} ]".colorize(Colour.BLUE)
        }

        val Footer: PrintableTemplate<TaskData> = PrintableTemplate {
            prefix.invoke(this, "Stop") +
                    " | $currentTime] Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
        }

        val Message: PrintableTemplate<TaskData> = PrintableTemplate {
            "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
        }

        val Debug: PrintableTemplate<TaskData> = PrintableTemplate {
            "${prefix.invoke(this, "")} ${messageFormatter.invoke(this).colorize(Colour.GREEN) }"
        }
    }
}
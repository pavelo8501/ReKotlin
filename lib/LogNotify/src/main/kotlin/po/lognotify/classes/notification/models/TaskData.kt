package po.lognotify.classes.notification.models

import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.models.TaskKey
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.withIndention
import po.misc.data.helpers.withMargin
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import po.misc.data.styles.SpecialChars
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.time.ExecutionTimeStamp

data class TaskData(
    val taskKey: TaskKey,
    val config: TaskConfig,
    val timeStamp : ExecutionTimeStamp,
    val message: String,
    val severity: SeverityLevel,
): PrintableBase<TaskData>(Message){

    override val self: TaskData = this
    override val emitter : Identifiable = asIdentifiable(taskKey.taskName, taskKey.moduleName)

    init {
        addTemplate(Header, Footer, Message)
    }

    companion object: PrintableCompanion<TaskData>({TaskData::class}) {
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

        val Header: PrintableTemplate<TaskData> = PrintableTemplate("Header") {
            SpecialChars.NewLine.char + prefix.invoke(this, "Start") + "${config.actor.emptyOnNull("by ")}]".colorize(Colour.BLUE)
        }

        val Footer: PrintableTemplate<TaskData> = PrintableTemplate("Footer") {
            prefix.invoke(this, "Stop") +
                    " | $currentTime] Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
        }

        val Message: PrintableTemplate<TaskData> = PrintableTemplate("Message") {
            "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
        }

        val Exception: PrintableTemplate<TaskData> = PrintableTemplate("Exception") {
            "${prefix.invoke(this, "")} ${messageFormatter.invoke(this)}"
        }

        val Debug: PrintableTemplate<TaskData> = PrintableTemplate("Debug", SpecialChars.NewLine.char,
            {aux-> "${Emoji.HammerAndPick}  ${(aux.prefix?:"N/A").colorize(Colour.BLUE)}".withMargin(1,0) },
            { message.withIndention(4," ").withMargin(0,1)}
        )

    }
}
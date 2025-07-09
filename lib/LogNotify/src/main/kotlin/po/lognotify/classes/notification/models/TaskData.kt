package po.lognotify.classes.notification.models

import po.lognotify.TaskProcessor
import po.lognotify.classes.action.ActionSpan
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.helpers.withIndention
import po.misc.data.helpers.withMargin
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import po.misc.data.styles.SpecialChars
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule
import po.misc.time.ExecutionTimeStamp


data class LogData(
    override val emitter : TaskProcessor,
    val config: TaskConfig,
    val timeStamp : ExecutionTimeStamp,
    val message: String,
    val severity: SeverityLevel,
): PrintableBase<LogData>(Message){

    override val self: LogData = this

    var nestingLevel: Int = 0

    private val nestingStr: String get() {
       return if(nestingLevel == 0){
            "R"
        }else{
            nestingLevel.toString()
       }
    }


    val prefix: String = when(emitter){
        is TaskBase<*,*> ->{
            nestingLevel = emitter.key.nestingLevel
            "[${emitter.key.taskName} ($nestingStr) | ${emitter.key.moduleName}]"
        }
        is ActionSpan<*> ->{
            nestingLevel =  emitter.inTask.nestingLevel
            emitter.contextName
        }
        else -> {
            emitter.contextName
        }
    }

    init {
        addTemplate(Header, Footer, Message)
    }

    companion object: PrintableCompanion<LogData>({LogData::class}) {

        val nestingFormatter: LogData.() -> String = {
            matchTemplate(
                templateRule(nestingLevel.toString()) { nestingLevel > 0 },
                templateRule("Root ".colorize(Colour.GREEN)) { nestingLevel == 0 }
            )
        }

        val messageFormatter: LogData.() -> String = {
            matchTemplate(
                templateRule(message) { severity == SeverityLevel.INFO },
                templateRule(message.colorize(Colour.YELLOW)) { severity == SeverityLevel.WARNING },
                templateRule(message.colorize(Colour.RED)) { severity == SeverityLevel.EXCEPTION }
            )
        }

        val Header: PrintableTemplate<LogData> = PrintableTemplate("Header") {
            prefix.colorize(Colour.BLUE)
        }

        val Footer: PrintableTemplate<LogData> = PrintableTemplate("Footer") {
            prefix.colorize(Colour.BLUE) + " | $currentTime] Elapsed: ${timeStamp.elapsed}".colorize(Colour.BLUE)
        }

        val Message: PrintableTemplate<LogData> = PrintableTemplate("Message") {
            prefix.colorize(Colour.BLUE) +  messageFormatter.invoke(this)
        }

        val Exception: PrintableTemplate<LogData> = PrintableTemplate("Exception") {
            prefix.colorize(Colour.BLUE) +  messageFormatter.invoke(this)
        }

        val Debug: PrintableTemplate<LogData> = PrintableTemplate("Debug", SpecialChars.NewLine.char,
            {"${Emoji.HammerAndPick}  ${prefix.colorize(Colour.BLUE)}".withMargin(1,0) },
            { message.withIndention(4," ").withMargin(0,1)}
        )

    }
}
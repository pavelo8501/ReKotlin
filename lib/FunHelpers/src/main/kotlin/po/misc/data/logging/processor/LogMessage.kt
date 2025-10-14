package po.misc.data.logging.processor

import po.misc.data.helpers.toStringIfNotNull
import po.misc.data.logging.LogEmitter
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.functions.dsl.helpers.nextBlock

class LogMessage(
    override val className: String,
    override val methodName: String?,
    override val classID: Long,
    override val severity: SeverityLevel,
    val subject: String,
    val message: String,
) : PrintableBase<LogMessage>(this), LogMeta{

    var parentContext: LogEmitter? = null

    override val self: LogMessage = this
    override val time: String = dateFormated(nowLocalDateTime())

    init {
        setDefaultTemplate(Message)
    }

    companion object: PrintableCompanion<LogMessage>({LogMessage::class}){
        val Message: Template<LogMessage> = createTemplate {
            nextLine {
                val methodName = methodName.toStringIfNotNull("") { "Exec : $it" }
                "[$className # $classID $methodName @ $time] -> ".colorize(Colour.Blue) + subject
            }
            nextLine {
                message
            }
        }
        val Warning: Template<LogMessage> = createTemplate {
            nextLine {
                val methodName = methodName.toStringIfNotNull("") { "Exec : $it" }
                "[$className # $classID $methodName @ $time] -> ".colorize(Colour.Blue) + subject.colorize(Colour.Yellow)
            }
            nextLine {
                message.colorize(Colour.Yellow)
            }
        }
    }
}


package po.misc.data.logging.models


import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import po.misc.data.styles.colorize
import po.misc.functions.dsl.helpers.nextBlock

class ContextMessage(
    val contextName: String,
    val methodName: String,
    val message: String,
    val severityLevel: SeverityLevel,
) : PrintableBase<ContextMessage>(this) {

    override val self: ContextMessage
        get() = this

    val colorizedMessage: String
        get() {
            return when (severityLevel) {
                SeverityLevel.INFO -> message.colorize(Colour.MAGENTA)
                SeverityLevel.WARNING -> "Method Name: $methodName $message".colorize(Colour.BRIGHT_YELLOW)
                SeverityLevel.EXCEPTION -> message.colorize(Colour.BRIGHT_RED)
                else -> message
            }
        }

    companion object : PrintableCompanion<ContextMessage>({ ContextMessage::class }) {
        val Message: Template<ContextMessage> = createTemplate {
            nextBlock {
                val header = "[$contextName  @ $currentDateTime] ->".colorize(Colour.BLUE)
                "$header $colorizedMessage"
            }
        }
        val Warning: Template<ContextMessage> = createTemplate {
            nextBlock {
                val header ="${Emoji.WARNING}[$contextName @ $currentDateTime] ->".colorize(Colour.BLUE)
                "$header  $colorizedMessage"
            }
        }
    }
}
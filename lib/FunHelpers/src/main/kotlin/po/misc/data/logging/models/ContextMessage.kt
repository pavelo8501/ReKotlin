package po.misc.data.logging.models


import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colorizer
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import po.misc.data.styles.colorize
import po.misc.functions.dsl.helpers.nextBlock
import po.misc.types.token.TypeToken

class ContextMessage(
    val contextName: String,
    val methodName: String,
    val subject: String,
    val message: String,
    val severityLevel: SeverityLevel,
) : PrintableBase<ContextMessage>(this), Colorizer{

    override val self: ContextMessage get() = this

    val colorizedMessage: String
        get() {
            return when (severityLevel) {
                SeverityLevel.INFO -> message.colorize(Colour.Magenta)
                SeverityLevel.WARNING -> "Method Name: $methodName $message".colorize(Colour.YellowBright)
                SeverityLevel.EXCEPTION -> message.colorize(Colour.RedBright)
                else -> message
            }
        }

//    val badge: String = BGColour.makeOfColour(BGColour.Yellow, Colour.BlackBright, "DEBUG")

    companion object : PrintableCompanion<ContextMessage>(TypeToken.create()) {

        val Message: Template<ContextMessage> = createTemplate {
            nextBlock {
                val header = "[$contextName  @ ${nowLocalDateTime()}] ->".colorize(Colour.Blue)
                "$header $colorizedMessage"
            }
        }
        val Warning: Template<ContextMessage> = createTemplate {
            nextBlock {
                val header ="${Emoji.WARNING}[$contextName @ ${nowLocalDateTime()}] ->".colorize(Colour.Blue)
                "$header  $colorizedMessage"
            }
        }

        val Debug: Template<ContextMessage> = createTemplate {
            nextLine {
                val debugBadge = colorize(BGColour.Yellow, Colour.BlackBright){ "DEBUG" }
                debugBadge + " [$contextName @ ${nowLocalDateTime()}]".colorize(Colour.Blue)
            }
            nextLine {
               "${colour("Method:", Colour.Cyan)}  $methodName"
            }
            nextLine {
               "${colour("Topic:", Colour.Cyan)} $subject"
            }
            nextLine {
                "${colour("Message:", Colour.Cyan)} $colorizedMessage"
            }
        }
    }
}
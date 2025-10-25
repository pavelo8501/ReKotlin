package po.misc.data.logging.models

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.printable.Printable
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.time.TimeHelper
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import java.time.Instant
import kotlin.reflect.KClass


/**
 * Default implementation for [Loggable]
 */
data class Notification(
    override val context: TraceableContext,
    override val topic: NotificationTopic,
    override val subject: String,
    override val text: String
): Loggable, TimeHelper, PrettyPrint {

    constructor(loggable: Loggable):this(loggable.context, loggable.topic, loggable.subject,loggable.text)

    private val contextName: String  = ClassResolver.instanceName(context)
    override val created: Instant = Instant.now()

    override val formattedString: String get() {
        return "[$contextName @ ${created.hoursFormated(3)}] -> $subject".applyColour(Colour.Blue).newLine {
            text.colorize(Colour.WhiteBright)
        }
    }

    companion object: Tokenized<Notification>{
        override val typeToken: TypeToken<Notification> = TypeToken.create()
    }

}
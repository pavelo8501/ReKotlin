package po.misc.data.logging.models

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
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
    override val subject: String,
    override val text: String,
    override val topic: NotificationTopic,
): Loggable, TimeHelper, PrettyPrint {

    constructor(loggable: Loggable):this(loggable.context, loggable.subject,loggable.text, loggable.topic)

    private val contextName: String  = ClassResolver.instanceName(context)
    override val created: Instant = Instant.now()

    private val formattedText: String get() {
       return when(topic){
            NotificationTopic.Info,  NotificationTopic.Debug ->  text.colorize(Colour.WhiteBright)
            NotificationTopic.Warning -> text.colorize(Colour.Yellow)
            NotificationTopic.Exception -> text.colorize(Colour.Red)
        }
    }

    override val formattedString: String get() {
        return "[$contextName @ ${created.hoursFormated(3)}] -> $subject".colorize(Colour.Blue).newLine {
            formattedText
        }
    }

    companion object: Tokenized<Notification>{
        override val typeToken: TypeToken<Notification> = TypeToken.create()
    }

}
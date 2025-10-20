package po.misc.context.tracable

import po.misc.context.component.Component
import po.misc.data.PrettyPrint
import po.misc.data.helpers.output
import po.misc.data.logging.LogRecord
import po.misc.data.printable.Printable
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.handling.Suspended
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.throwableToText
import po.misc.time.TimeHelper
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import java.time.Instant
import kotlin.reflect.KClass



enum class NotificationTopic(val value: Int): Comparable<NotificationTopic>{
    Debug(0),
    Info(1),
    Warning(2),
    Exception(3)
}

data class Notification(
    override val context: TraceableContext,
    override val topic: NotificationTopic,
    override val subject: String,
    override val text: String,

): LogRecord, PrettyPrint, TimeHelper{

    val created: Instant = nowTimeUtc()

    private val contextName: String get() {
      return  when (context){
            is Component -> context.componentID.formattedString
            else -> {
                ClassResolver.classInfo(context).simpleName
            }
        }
    }
    override val formattedString: String
        get() = "[$contextName @ ${created.hoursFormated(3)}] -> $subject".applyColour(Colour.Blue).newLine {
            text.colorize(Colour.WhiteBright)
        }

    override val ownClass: KClass<Notification> = Notification::class
    override val arbitraryMap: ArbitraryDataMap<Printable> = ArbitraryDataMap()

    override fun echo() {
        println(formattedString)
    }
    override fun toString(): String = "$contextName [${topic.name}]"

    companion object: Tokenized<Notification>{
        override val typeToken: TypeToken<Notification> = TypeToken.create()
    }
}


interface TraceableContext {

    fun notify(topic: NotificationTopic, subject: String, text: String):LogRecord{
       val notification = Notification(this, topic, subject, text)
       notification.output()
       return  notification
    }

    fun notify(subject: String, throwable: Throwable):LogRecord{
        val notification = Notification(this, NotificationTopic.Exception, subject, throwable.throwableToText())
        notification.output()
        return notification
    }

    fun info(subject: String, text: String):LogRecord = notify(NotificationTopic.Info, subject, text)
    fun warn(subject: String, text: String): LogRecord = notify(NotificationTopic.Info, subject, text)
    fun warn(subject: String, throwable: Throwable): LogRecord = notify(subject, throwable)


    /**
     * Context-bound shorthand for [getOrThrow], automatically using the current [TraceableContext].
     *
     * This makes null validation fluent within traceable components, avoiding the need to explicitly
     * pass `context` each time. When invoked, this delegates to the top-level `getOrThrow`, enriching
     * the resulting [ExceptionPayload] with the owning context instance.
     *
     * @param expectedClass Optional explicit type for error clarity. Defaults to inferred class of `T`.
     * @param exceptionProvider A factory receiving the [ExceptionPayload] and producing the throwable.
     *
     * @return The receiver if non-null.
     *
     * @throws Throwable As created by [exceptionProvider], if the receiver is `null`.
     */
    fun <T: Any> T?.getOrThrow(
        expectedClass: KClass<T>? = null,
        exceptionProvider: (ExceptionPayload)-> Throwable
    ):T = getOrThrow(this@TraceableContext, expectedClass, exceptionProvider)


    /**
     * Context-bound shorthand for [castOrThrow], automatically binding this [TraceableContext]
     * as exception origin.
     *
     * @param kClass Explicit target type to cast the receiver to.
     * @param exceptionProvider Factory for converting [ExceptionPayload] to a thrown exception.
     *
     * @return The successfully casted receiver.
     *
     * @throws Throwable If `null` or incompatible type.
     */
    fun <T: Any> Any?.castOrThrow(
        kClass: KClass<T>,
        exceptionProvider: (ExceptionPayload)-> Throwable,
    ): T = castOrThrow(this@TraceableContext, kClass, exceptionProvider)
}

object NonResolvable: TraceableContext


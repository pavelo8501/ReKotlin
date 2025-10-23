package po.misc.context.tracable

import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.throwableToText
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import kotlin.reflect.KClass

interface TraceableContext {


    fun notify(notification: Loggable){
        notification.output()
    }

    fun notify(topic: NotificationTopic, subject: String, text: String): Loggable {
       val notification = Notification(this, topic, subject, text)
       notify(notification)
       return notification
    }

    fun notify(subject: String, throwable: Throwable): Loggable =
        notify(NotificationTopic.Exception, subject, throwable.throwableToText())

    fun info(subject: String, text: String): Unit {
        notify(NotificationTopic.Info, subject, text)
    }
    fun debug(subject: String, text: String): Unit {
        notify(NotificationTopic.Debug, subject, text)
    }
    fun warn(subject: String, text: String): Unit {
        notify(NotificationTopic.Info, subject, text)
    }
    fun warn(subject: String, throwable: Throwable): Unit {
        notify(subject, throwable)
    }

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
    ): T = getOrThrow(this@TraceableContext, expectedClass, exceptionProvider)

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


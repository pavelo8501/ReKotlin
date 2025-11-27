package po.misc.context.tracable

import po.misc.data.output.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.exceptions.ExceptionPayload
import po.misc.debugging.stack_tracer.StackTracer
import po.misc.exceptions.throwableToText
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import kotlin.reflect.KClass

/**
 * A base interface representing an execution context capable of emitting traceable events.
 *
 * `TraceableContext` defines a common notification and logging layer for components that
 * participate in observable or traceable processes. It provides structured methods for
 * emitting messages, warnings, exceptions, and other diagnostic events.
 *
 * ### Behavior
 * By default, [notify] immediately calls [Loggable.output], which prints the message to
 * the console or the default output sink. However, implementations are encouraged to
 * override [notify] to introduce context-specific behavior — for instance:
 *
 * - Filtering messages by verbosity level
 * - Redirecting output to a logging subsystem
 * - Buffering or aggregating notifications
 *
 * Overriding **any** `notify` method effectively breaks the default "direct output" chain
 * and transfers responsibility for message delivery to the implementation.
 * This is intentional and provides powerful customization hooks.
 *
 * ### Example
 * ```kotlin
 * class VerboseComponent : TraceableContext {
 *     override fun notify(loggable: Loggable) {
 *         if (loggable.topic.priority >= NotificationTopic.Info.priority) {
 *             loggable.output() // only emit info and above
 *         }
 *     }
 * }
 * ```
 *
 * @see Loggable
 * @see Notification
 * @see NotificationTopic
 */
interface TraceableContext  : StackTracer {

    fun notification(subject: String, text: String, topic: NotificationTopic = NotificationTopic.Info): Notification{
        return Notification(this, subject, text, topic)
    }

    /**
     * Emits a [Loggable] event to the output.
     *
     * By default, this calls [Loggable.output], printing the message immediately.
     * Overriding this method allows intercepting or transforming notifications
     * before they are output, or redirecting them to a custom destination.
     *
     * **Note:** Overriding this method replaces the default behavior — the message
     * will no longer be printed to the console unless you explicitly call
     * [Loggable.output] within your override.
     */
    fun notify(loggable: Loggable):Loggable{
        loggable.output()
        return loggable
    }

    /**
     * Creates and emits a [Notification] with a given [topic], [subject], and [text].
     * Returns the created [Loggable] instance.
     */
    fun notify(subject: String, text: String, topic: NotificationTopic = NotificationTopic.Info): Loggable {
       val notification = Notification(this,  subject, text, topic)
       notify(notification)
       return notification
    }

    fun notify(
        outputImmediately: Boolean,
        subject: String,
        text: String,
        topic: NotificationTopic = NotificationTopic.Info
    ): Loggable {
        val notification = Notification(this,  subject, text, topic)
        if(outputImmediately){
            notification.output()
        }else{
            notify(notification)
        }
        return notification
    }

    /**
     * Emits an exception trace as a [NotificationTopic.Exception].
     * The [Throwable] is converted into a formatted text trace automatically.
     */
    fun notify(subject: String, throwable: Throwable): Loggable =
        notify(subject, throwable.throwableToText(), NotificationTopic.Exception)


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


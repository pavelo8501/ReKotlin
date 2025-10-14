package po.misc.context

import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.handling.Suspended
import po.misc.exceptions.metaFrameTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import kotlin.reflect.KClass

interface TraceableContext  {
    object NonResolvable: TraceableContext


    fun info(subject: String, text: String){
        "[Subject:$subject] $text".output(Colour.WhiteBright)
    }

    fun warn(subject: String, text: String) {
        "[Subject:$subject] $text".output(Colour.Yellow)
    }

    fun warn(throwable: Throwable) {
        val trace = throwable.extractTrace(this)
        trace.output()
    }

    suspend fun warn(suspended: Suspended, throwable: Throwable) {
        throwable.output()
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


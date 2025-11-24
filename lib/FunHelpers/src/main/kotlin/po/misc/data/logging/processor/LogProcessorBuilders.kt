package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.context.log_provider.LogProvider
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken


@PublishedApi
 internal fun <H: Component, T: StructuredLoggable> newLogProcessor(
    host: H,
    token:  TypeToken<T>
 ): LogProcessor<H, T> {
     return  LogProcessor(host, token)
 }


/**
 * Creates a [LogProcessor] for this [LogProvider], inferring record type [LR]
 * directly from the provider's generic parameter.
 *
 * Use this when implementing `LogProvider<LR>`, as the log record type is
 * already known and does not need to be provided explicitly.
 *
 * ### Example
 * ```
 * class MyService : LogProvider<MyLogEntry> {
 *     private val logger = logProcessor()
 * }
 * ```
 *
 * @receiver A component that implements [LogProvider].
 * @return A new [LogProcessor] bound to this provider.
 */

//inline fun <H: LogProvider, reified LR: StructuredLoggable> H.createLogProcessor(): LogProcessor<H, LR> =
//    newLogProcessor(this, TypeToken.create<LR>())


/**
 * Creates a [LogProcessor] for a plain [Component] using an explicit [TypeToken].
 *
 * Use this overload when the component does **not** implement [LogProvider],
 * but you still want to produce custom log records of type [LR].
 *
 * ### Example
 * ```
 * val logger = someComponent.logProcessor(TypeToken.create<MyLogEntry>())
 * ```
 *
 * @param typeToken Token describing the concrete log record type.
 * @receiver A component that will host the processor.
 */
fun <H: Component, LR: StructuredLoggable> H.createLogProcessor(
    typeToken: TypeToken<LR>
): LogProcessor<H, LR> = newLogProcessor(this,  typeToken)

/**
 * Creates a [LogProcessor] using a [Tokenized] companion to resolve the record type.
 *
 * Preferred overload when your log record type implements `Tokenized<LR>`,
 * providing stronger type safety and cleaner syntax.
 *
 * ### Example
 * ```
 * data class Notification(...) : LogRecord, Tokenized<Notification> {
 *     companion object : Tokenized<Notification>
 * }
 *
 * val logger = component.logProcessor(Notification)
 * ```
 *
 * @param tokenized The companion providing [TypeToken] for [LR].
 * @receiver A component serving as log host.
 */
fun <H: Component, LR: StructuredLoggable> H.createLogProcessor(
    tokenized: Tokenized<LR>
): LogProcessor<H, LR> = newLogProcessor(this, tokenized.typeToken)


fun <H: Component> H.createLogProcessor(): LogProcessor<H, LogMessage> =
    newLogProcessor(this, TypeToken.create<LogMessage>())





package po.misc.data.logging

import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.processors.SeverityLevel


/**
 * A flexible logging contract that provides default logging behavior for any component,
 * without requiring it to implement a specific context interface.
 *
 * This interface exposes two logging functions—`log()` for structured data and `notify()` for plain messages—
 * and delegates their actual behavior to overridable function properties. If no custom implementation is provided,
 * logs are simply printed to standard output.
 *
 * The use of `Any` as the emitter allows lightweight components to use logging without needing to
 * implement a full logging context. In more advanced systems, the emitter can be introspected or cast
 * to richer types (like `CTX`) at runtime.
 */
interface LogEmitter {

    /**
     * Function responsible for logging structured data (`PrintableBase`).
     *
     * By default, it calls [PrintableBase.echo] on the data object, effectively printing it to the console.
     * Can be overridden to forward logs to a centralized logger, file, or other logging sinks.
     *
     * @param data The structured data to log.
     * @param severity The severity level of the message.
     * @param emitter The object that emitted the log (may or may not be a [CTX]).
     *
     * @see PrintableBase
     */
    val datLogger: (PrintableBase<*>, SeverityLevel, Any) -> Unit get() =  {data, severity, context->
        data.echo()
    }

    /**
     * Function responsible for logging plain text messages.
     *
     * By default, it prints the message to standard output.
     * Can be overridden to implement richer behavior like filtering, formatting, or async logging.
     *
     * @param message The message to log.
     * @param severity The severity level of the message.
     * @param emitter The object that emitted the log (may or may not be a [CTX]).
     */
    val messageLogger: (String, SeverityLevel, Any) -> Unit get() =  { message, _, _ ->
        println(message)
    }

    /**
     * Emits a plain text log message from any object.
     *
     * This is a convenience extension and does not require the caller to implement any interface.
     * Logging backends may inspect the emitter type if contextual data is required.
     *
     * @param message The message to be logged.
     * @param severity The severity level of the log message (defaults to [SeverityLevel.INFO]).
     */
    fun Any.notify(message: String, severity: SeverityLevel = SeverityLevel.INFO){
        messageLogger.invoke(message, severity, this@notify)
    }

    /**
     * Emits a structured log entry from any object, using a [PrintableBase] instance.
     *
     * This is a convenience extension and does not require the caller to implement any interface.
     * Logging backends may inspect the emitter type if contextual data is required.
     *
     * @param data The structured data to be logged.
     * @param severity The severity level of the log message (defaults to [SeverityLevel.LOG]).
     */
    fun <T: PrintableBase<T>> Any.log(data: T, severity: SeverityLevel = SeverityLevel.LOG){
        datLogger.invoke(data, severity, this@log)
    }
}
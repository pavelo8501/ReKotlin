package po.misc.data.processors

import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase


/**
 * A common contract for logging printable data using optional templates.
 *
 * This interface defines a generic logging mechanism for any type that conforms to [PrintableBase].
 * It allows for flexible output formatting by optionally supplying a [PrintableTemplateBase] that defines
 * how the data should be rendered.
 */
interface Logger{

    /**
     * Logs the given [data] instance of type [T], optionally formatted with the specified [template].
     *
     * @param T The type of the data to be logged. It must implement [PrintableBase] with a self-referencing generic.
     * @param data The instance to be logged.
     * @param template An optional [PrintableTemplateBase] that customizes the formatting of [data].
     *                 If `null`, a default or implicit format may be used.
     */

    fun <T: PrintableBase<T>> CTX.log(data: T, severity: SeverityLevel = SeverityLevel.INFO)
}

/**
 * A common contract for receiving printable data with optional formatting context.
 *
 * This interface represents any component that handles incoming log data,
 * potentially applying a given [PrintableTemplateBase] to interpret or validate the input.
 */
interface LogReceiver {
    /**
     * Receives and processes the given [data].
     * @param data The loggable data of subtype [PrintableBase] being received.
     */
    fun receive(data: PrintableBase<*>)
}


interface LogExchange<out T: PrintableBase<out T>>{

    fun log(data: @UnsafeVariance T, template: PrintableTemplateBase<@UnsafeVariance T>? = null)
    fun receive(data: PrintableBase<*>)
}

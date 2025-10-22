package po.misc.data.logging

import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic
import po.misc.exceptions.ExceptionLocatorBase
import po.misc.exceptions.HelperPackage


/**
 * Interface for context-aware components that participate in logging and tracing.
 *
 * ## Purpose
 * - Extends [CTX] and [LogEmitter] to provide full context-aware logging capabilities.
 * - Requires an [emitter] of type [ContextAwareLogEmitter] to handle all log operations.
 * - Provides convenience functions to log messages at different levels directly from the context.
 *
 * ## Logging Methods
 * - [info(message)] → Logs an informational message.
 * - [warn(message)] → Logs a warning message.
 * - [notify(message, severity)] → Logs a message with a custom severity.
 * - [log(data, severity)] → Logs structured [PrintableBase] data.
 * - [debug(message, template, topic)] → Logs debug information with optional templates.
 *
 * ## ExceptionLocator
 * - A singleton object that helps locate relevant stack frames in exceptions.
 * - Filters out helper / framework packages to identify user code locations.
 * - [helperPackages] contains default packages that are considered “helpers”:
 *   - `"po.misc"`
 *   - `"kotlin"`
 *   - `"java"`
 *
 * ## Example Usage
 * ```
 * class TestContextAware : ContextAware {
 *     override val identity: CTXIdentity<TestContextAware> = asIdentity()
 *     override val emitter: ContextAwareLogEmitter = logEmitter()
 *
 *     init {
 *         identity.setNamePattern { "TestContextAware(Something)" }
 *     }
 *
 *     fun doSomething() {
 *         info("Doing work")
 *         warn("Something minor happened")
 *         notify("Custom severity message", SeverityLevel.WARNING)
 *     }
 * }
 * ```
 *
 * Notes:
 * - The [emitter] is central: all logging functions delegate to it.
 * - This design allows logging and trace behavior to be centralized and consistent across all context-aware classes.
 */
interface ContextAware : LogEmitter, CTX {

    val emitter: ContextAwareLogEmitter

    val emitterSafe: ContextAwareLogEmitter get() {
        @Suppress("UNNECESSARY_SAFE_CALL", "REDUNDANT_NULLABLE")
       return emitter?.let {
            it
        }?:run {
            FakeCtx.emitter
        }
    }

//    fun info(message: String){
//        emitter.info(message)
//    }
//
//    fun warn(message: String){
//        emitter.warn(message)
//    }


    override fun Any.notify(message: String, severity: SeverityLevel) {
        emitter.notify(message,  severity)
    }
    override fun Any.log(data: PrintableBase<*>, severity: SeverityLevel){
        emitter.log(data, severity)
    }
    override fun <T: Printable> CTX.debug(message: String, template: PrintableTemplateBase<T>?, topic: DebugTopic){
        emitter.debug("", message, topic)
    }

    fun debug(methodName: String,  message: String, topic: DebugTopic = DebugTopic.General){
        emitter.debug(methodName, message, topic)
    }

    object ExceptionLocator : ExceptionLocatorBase(){

        override val helperPackages: MutableList<HelperPackage> = mutableListOf(
            HelperPackage("po.misc"),
            HelperPackage("kotlin"),
            HelperPackage("java")
        )
    }
}

internal object FakeCtx: ContextAware{
    override val identity: CTXIdentity<FakeCtx> = asIdentity()
    override val emitter: ContextAwareLogEmitter = ContextAwareLogEmitter(this)
}

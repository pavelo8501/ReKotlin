package po.misc.data.logging

import po.misc.data.helpers.output
import po.misc.data.logging.models.ContextMessage
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic
import po.misc.exceptions.metaFrameTrace
import po.misc.types.helpers.simpleOrNan


/**
 * Context-aware log emitter responsible for all logging operations within a [ContextAware] host.
 *
 * ## Purpose
 * - Centralizes logging for [ContextAware] instances.
 * - Provides convenience methods for logging at different severity levels:
 *   - [info] → Informational messages
 *   - [warn] → Warning messages (captures stack trace, more costly)
 *   - [debug] → Debug messages (can be extended with topics)
 *   - [notify] → Dispatches messages based on severity
 *
 * ## Notes on performance
 * - The [warn] method is relatively expensive because it:
 *   1. Captures a snapshot of the current stack trace via [awareHost.metaFrameTrace].
 *   2. Selects the most relevant stack frame for reporting (`trace.bestPick`).
 *   3. Constructs a detailed [ContextMessage] including context name and method.
 *
 * - Use [warn] judiciously in hot code paths to avoid unnecessary performance overhead.
 *
 * ## Construction
 * - Typically created via the [ContextAware.logEmitter] helper function:
 * ```
 * val emitter = context.logEmitter {
 *     level = SeverityLevel.INFO
 *     enableStackTrace = true
 * }
 * emitter.info("Starting work")
 * emitter.warn("Potential problem detected")
 * ```
 *
 * ## Properties
 * - [awareHost] → The [ContextAware] instance this emitter belongs to.
 * - [contextName] → Readable name for logging, includes numeric ID and identified name.
 *
 * ## Internal helpers
 * - [createWarningData] → Builds a [ContextMessage] for warnings including method name.
 * - [createData] → Builds a [ContextMessage] for informational messages.
 *
 * ## Usage of `notify`
 * - Dispatches messages to the appropriate logging function based on [SeverityLevel].
 * - Maps WARNING → [warn], INFO → [info], others → prints directly.
 */
class ContextAwareLogEmitter(
    override val host: ContextAware,
    configurator:(EmitterConfig.()-> Unit)? = null
): LogEmitterClass(host, configurator){

    val contextName: String get() = "[${host.identifiedByName}]#${host.identity.numericId}"

    init {
        configurator?.invoke(config)
    }


    private fun createData(severityLevel: SeverityLevel, methodName: String,  message: String): ContextMessage{
        return ContextMessage(
            contextName = contextName,
            methodName = methodName,
            message = message,
            subject = "",
            severityLevel = severityLevel
        )
    }

    private fun createData(severityLevel: SeverityLevel, methodName: String, subject: String,  message: String): ContextMessage{
        return ContextMessage(
            contextName = contextName,
            methodName = methodName,
            subject = subject,
            message = message,
            severityLevel = severityLevel
        )
    }

    /** Logs a warning message with detailed stack info. Relatively expensive. */
    override fun warn(message: String){
        val trace = host.metaFrameTrace()
        val data =   createData(SeverityLevel.WARNING, trace.bestPick.methodName, message)
        data.setDefaultTemplate(ContextMessage.Warning)
        data.echo()
    }


   private  fun infoWithResolution (message: String,  producedBy: Any):ContextMessage{
     val name =  when(producedBy){
           is ContextAware ->{
               producedBy.identifiedByName
           }
           is Any->{
               "${producedBy::class.simpleOrNan()}  on behalf of $contextName"
           }
       }
      return  ContextMessage(
            contextName = name,
            methodName = "",
            subject = "",
            message = message,
            severityLevel = SeverityLevel.INFO
        )
    }

    override fun info(message: String){
       val data = createData(SeverityLevel.INFO, "", message)
        data.setDefaultTemplate(ContextMessage.Message)
        data.echo()
    }

    fun debug(methodName: String, message: String,  topic: DebugTopic = DebugTopic.General){
        val data =  ContextMessage(contextName, methodName, topic.name, message, SeverityLevel.DEBUG)
        data.setDefaultTemplate(ContextMessage.Debug)
        config.onMessageCallback?.invoke(data)
        data.echo()
    }

    fun <T: Any> notify2(receiver: T, message: String) {
        val data = infoWithResolution(message, receiver)
        data.output()
    }

    fun notify(message: String, severityLevel: SeverityLevel) {
        when(severityLevel){
            SeverityLevel.WARNING-> warn(message)
            SeverityLevel.INFO -> info(message)
            else -> {
                message.output()
            }
        }
    }
}

/**
 * Helper function to create a [ContextAwareLogEmitter] for a given [ContextAware].
 *
 * ## Purpose
 * - Provides a convenient way to initialize the [emitter] property in context-aware classes.
 * - Accepts an optional [configurator] lambda to customize the emitter behavior via [EmitterConfig].
 *
 * ## Usage Example
 * ```
 * class TestContextAware : ContextAware {
 *     override val identity: CTXIdentity<TestContextAware> = asIdentity()
 *     override val emitter: ContextAwareLogEmitter = logEmitter {
 *         // optional custom configuration
 *         level = SeverityLevel.DEBUG
 *         enableStackTrace = true
 *     }
 * }
 * ```
 *
 * @param configurator Optional lambda to configure the emitter via [EmitterConfig].
 * @return A new instance of [ContextAwareLogEmitter] bound to this context.
 */
fun ContextAware.logEmitter(
    configurator:(EmitterConfig.()-> Unit)? = null
):ContextAwareLogEmitter{
    return ContextAwareLogEmitter(this, configurator)
}

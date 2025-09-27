package po.misc.data.logging.processor


import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.logging.EmitterConfig
import po.misc.data.logging.LogEmitter
import po.misc.data.logging.Verbosity
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import po.misc.types.helpers.simpleOrNan


class LogProcessor(
    val host: LogEmitter,
    val parentProcessor: DataProcessorBase<LogMessage>? = null,
    builder:(EmitterConfig.()-> Unit)? = null
): DataProcessorBase<LogMessage>(parentProcessor){

    private val config: EmitterConfig = EmitterConfig()

    init {
        builder?.invoke(config)
        composeBaseHooks()
    }

    private fun composeBaseHooks(){
        hooks.onSubDataReceived {message, processor->
            message.parentContext = host
        }
    }

    private fun resolveClassName(context: Any): String {
        return when (context) {
            is CTX -> context.identifiedByName
            else -> context::class.simpleOrNan()
        }
    }
    private fun resolveClassID(context: Any): Long {
        return when (context) {
            is CTX -> context.identity.numericId
            else -> context.hashCode().toLong()
        }
    }

    private fun extractFromException(throwable: Throwable, debugTopic: DebugTopic?):LogMessage {

        var className: String? = null
        var exceptionText = throwable.throwableToText()
        var methodName: String = "N/A"
        var classID: Long? = null

        when (throwable) {
            is ManagedException -> {
                className = resolveClassName(throwable.context)
                methodName = throwable.exceptionTrace.bestPick.methodName
                classID = resolveClassID(throwable.context)
                throwable.code?.let {
                    exceptionText += "Code : ${it.name}"
                }
            }
        }
        return LogMessage(
            className = className ?: resolveClassName(host),
            methodName = methodName,
            classID = classID ?: resolveClassID(host),
            severity = SeverityLevel.EXCEPTION,
            subject = debugTopic?.name ?: "Exception",
            message = exceptionText
        )
    }

    fun info(message: String, subject: String? = null) {
        val logMessage = LogMessage(
            className = resolveClassName(host),
            methodName = "N/A",
            classID = resolveClassID(host),
            severity = SeverityLevel.INFO,
            subject = subject ?: "N/A",
            message = message
        )
        addData(logMessage)
        if(config.verbosity == Verbosity.Info){
            logMessage.output()
        }
    }

    fun warn(message: String, subject: String? = null) {
        val logMessage = LogMessage(
            className = resolveClassName(host),
            methodName = "N/A",
            classID = resolveClassID(host),
            severity = SeverityLevel.WARNING,
            subject = subject ?: "N/A",
            message = message
        )
        addData(logMessage)
        logMessage.output()
    }

    fun log(throwable: Throwable, debugTopic: DebugTopic? = null){
        val logMessage =  extractFromException(throwable, debugTopic)
        addData(logMessage)
        logMessage.output()
    }

}
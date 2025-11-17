package po.misc.data.logging

import po.misc.context.component.Component
import po.misc.data.logging.log_subject.InfoSubject
import po.misc.data.logging.log_subject.LogSubject
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.parts.LogTracker
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.processor.LogProcessor


/**
 * Declares a component capable of producing structured log records of type [LR].
 *
 * A `LogProvider` represents any component that *originates* log entries
 * rather than just consuming them. It extends [Component] and provides the
 * generic log type used by its associated [LogProcessor].
 *
 * ### Typical Role
 * Implement this interface when a class must:
 * - Create and own a logging context.
 * - Emit log records with custom payload (e.g. notifications, procedural logs).
 * - Use `logProcessor()` without explicitly passing a `TypeToken`.
 *
 * @param LR The concrete type of log record produced by this provider.
 *
 * @see LogProvider for log collection and dispatch.
 * @see Component for identity and verbosity management.
 */
interface LogProvider<LR: StructuredLoggable>: Component{


}

interface LogEmitterNew<H: Component, LR: StructuredLoggable>: Component{

    val logProcessor: LogProcessor<H, LR>

    override fun info(subject: String, text: String): LogMessage{
       val message = infoMsg(subject, text)
       logProcessor.log(message)
       return message
    }

    fun info(subject: InfoSubject, text: String): LogMessage{
        val message = infoMsg(subject, text)
        logProcessor.log(message)
        return message
    }

    override fun warn(subject: String, text: String, tracker: LogTracker): LogMessage{
        val warning = warning(subject, text, tracker)
        logProcessor.log(warning)
        return warning
    }

    fun warn(subject: String, throwable: Throwable, tracker: LogTracker = LogTracker.Enabled):LogMessage{
        val warning =  warning(subject, throwable, tracker)
        logProcessor.log(warning)
        return warning
    }
}


inline fun <SL:  StructuredLoggable, H: Component, R>  LogEmitterNew<H, SL>.proceduralScope(
    record: SL,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord)-> R
):R {
    val flow =  logProcessor.createProceduralFlow(record)
    val result =  block.invoke(flow, flow.proceduralRecord)
    logProcessor.finalizeFlow(record, flow)
    return result
}



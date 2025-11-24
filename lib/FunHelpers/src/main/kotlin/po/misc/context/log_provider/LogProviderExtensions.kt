package po.misc.context.log_provider

import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.log_subject.StartProcessSubject
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord


inline fun <H:LogProvider,  R> H.withProceduralScope(
    record: StructuredLoggable,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord)-> R
):R {
    val flow =  logProcessor.newProceduralFlow<H>(record)
    val result =  block.invoke(flow, flow.proceduralRecord)
    logProcessor.finalizeFlow(flow)
    return result
}

inline fun <H:LogProvider,  R> H.withProceduralScope(
    subject:  StartProcessSubject,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord)-> R
):R {
    val record = subject.toLogMessage()
    val flow =  logProcessor.newProceduralFlow<H>(record)
    val result =  block.invoke(flow, flow.proceduralRecord)
    logProcessor.finalizeFlow(flow)
    return result
}

fun <H:LogProvider> H.startProceduralLog(
    record: StructuredLoggable,
):  ProceduralFlow<H> {
    val flow =  logProcessor.newProceduralFlow<H>(record)
    return flow
}

fun <H:LogProvider> H.startProceduralLog(
    subject:  StartProcessSubject,
):  ProceduralFlow<H> = startProceduralLog(subject.toLogMessage())


@Deprecated("Change to withProceduralScope")
inline fun <H:LogProvider,  R> H.proceduralScope(
    record: StructuredLoggable,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord)-> R
):R = withProceduralScope(record, block)
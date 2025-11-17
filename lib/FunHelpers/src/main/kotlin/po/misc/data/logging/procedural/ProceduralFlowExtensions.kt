package po.misc.data.logging.procedural

import po.misc.context.component.Component
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.log_subject.InfoSubject

import po.misc.data.logging.processor.LogProcessor


@PublishedApi
internal inline fun <H: Component, SL: StructuredLoggable,  R> launchPage(
    ownFlow: ProceduralFlow<*>,
    foreignProcessor: LogProcessor<H, SL>,
    logMessage: LogMessage,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
): R {
    val ownProceduralRecord = ownFlow.proceduralRecord
    val record = ProceduralFlow.createRecord(ownProceduralRecord, logMessage)
    val result =  foreignProcessor.proceduralScope(record, block)
    return  result
}


inline fun <H: Component,    SL: StructuredLoggable,  R> ProcFlowHandler<H>.page(
    logProcessor: LogProcessor<H, SL>,
    logMessage: LogMessage,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
): R  = launchPage( (this as ProceduralFlow), logProcessor, logMessage, block)

inline fun <H: Component,  SL: StructuredLoggable, R> ProcFlowHandler<*>.page(
    logProcessor: LogProcessor<H, SL>,
    subject: String,
    text: String,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
): R  = launchPage((this as ProceduralFlow), logProcessor, infoMsg(subject, text),  block)


inline fun <H: Component,  SL: StructuredLoggable, R> ProcFlowHandler<*>.page(
    logProcessor: LogProcessor<H, SL>,
    infoSubject: InfoSubject,
    crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
): R = launchPage((this as ProceduralFlow), logProcessor, infoMsg(infoSubject, null), block)



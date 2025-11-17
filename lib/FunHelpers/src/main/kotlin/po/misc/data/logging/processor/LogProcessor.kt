package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.Verbosity
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord

import po.misc.data.styles.Colour
import po.misc.functions.Suspending
import po.misc.types.safeCast
import po.misc.types.token.TypeToken

class LogProcessor <H: Component, T: StructuredLoggable>(
    override val host: H,
    typeToken: TypeToken<T>,
): LogProcessorBase<T>(host, typeToken) {

    val loggerName: String get() = "LogProcessor of ${host.componentName}"

    private val dataProcessSubject = "Data Processing"
    private val impossibleToProcess: (String, String) -> String = { method, reason, ->
        "Unable to run $method. Reason: $reason"
    }
    private var dataBuilder: ((LogMessage) -> T)? = null

    private fun castReCreateOrnNull(logMessage: LogMessage): T? {
        val casted = logMessage.safeCast(typeToken) ?: run {
            dataBuilder?.invoke(logMessage)
        }
        return casted
    }

    @PublishedApi
    internal fun createProceduralFlow(proceduralRecord: ProceduralRecord): ProceduralFlow<H> {
        val flow = ProceduralFlow(this, proceduralRecord)
        useHandler(flow, LogMessage::class)
        return flow
    }

    @PublishedApi
    internal fun finalizeFlow(record: T, flow: ProceduralFlow<H>){
        val flowMessage = flow.complete(output = true)
        flowMessage.logRecords.forEach {
            record.addRecord(it)
        }
        removeDataHandler(LogMessage::class)
        dropCollector(true)
        if (verbosity == Verbosity.Debug) {
            StateSnapshot(this).output("Before record  $record logged")
        }
        logData(record, noOutput = true)
        if (verbosity == Verbosity.Debug) {
            StateSnapshot(this).output("After record  $record logged")
        }
    }

    fun finalizeFlow(flow: ProceduralFlow<H>):T{
      return  activeRecord?.let {
            finalizeFlow(it, flow)
            it
        }?:run {
            throw IllegalStateException("No activeRecord")
        }
    }

    fun createProceduralFlow(data: T): ProceduralFlow<H>{
        ProceduralFlow.createRecord(data)
        val flow = ProceduralFlow(this,  ProceduralFlow.createRecord(data))
        activeRecord = data
        return flow
    }

    inline fun <R> proceduralScope(
        record: T,
        crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R {
        val flow = createProceduralFlow(record)
        val result = block.invoke(flow, flow.proceduralRecord)
        finalizeFlow(record, flow)
        return result
    }

    inline fun <R> proceduralScope(
        proceduralRecord: ProceduralRecord,
        crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R {
        val flow = createProceduralFlow(proceduralRecord)
        val result = block.invoke(flow, flow.proceduralRecord)
        removeDataHandler(LogMessage::class)
        flow.proceduralRecord.calculateResult()
        return result
    }

    suspend fun <R> proceduralScope(
        suspending: Suspending,
        record: T,
        block: suspend ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R {
        val flow = createProceduralFlow(record)
        val result = block.invoke(flow, flow.proceduralRecord)
        finalizeFlow(record, flow)
        return result
    }

    fun useProcedural(proceduralFlow: ProceduralFlow<*>): Unit = useHandler(proceduralFlow, allowOverwrite = true)


    override fun outputOrNot(data: Loggable) {
        if (data.topic >= verbosity.minTopic) {
            data.output()
        }
    }

    override fun handleUnAssigned(message: Loggable) {
        val message = "$loggerName Unable to process loggable message of ${message::class}"
        message.output(Colour.Yellow)
    }
}



package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.data.output.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.Verbosity
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.processor.parts.ProcessorLoader
import po.misc.data.logging.processor.settings.StateSnapshot
import po.misc.data.styles.Colour
import po.misc.functions.Suspending
import po.misc.functions.Throwing
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken


class LogProcessor <H: Component, T: StructuredLoggable>(
    val host: H,
    messageTypeToken: TypeToken<T>,
    private val useHostName: String? = null
): LogProcessorBase<T>("LogProcessor",  messageTypeToken) {


    private val dataProcessSubject = "Data Processing"
    private val impossibleToProcess: (String, String) -> String = { method, reason, ->
        "Unable to run $method. Reason: $reason"
    }

    private var dataBuilder: ((LogMessage) -> T)? = null
    var loggerName: String = "LogProcessor"
        private set

    init {
        resolveNaming(useHostName)
    }

    override  var verbosity: Verbosity
        get() { return host.componentID.verbosity }
        set(value) {
            host.componentID.verbosity = value
        }

    val loader: ProcessorLoader<H, T> = ProcessorLoader(this)

    private fun resolveNaming(useHostName: String?){
        if(useHostName != null){
            hostName = useHostName
            loggerName = "LogProcessor of $useHostName"
        } else {
            loggerName = "LogProcessor of ${host.componentName}"
        }
    }

    @PublishedApi
    internal fun createProceduralFlow(proceduralRecord: ProceduralRecord): ProceduralFlow<H> {
        val flow = ProceduralFlow(this, proceduralRecord)
        useHandler(flow, LogMessage::class)
        return flow
    }

    @PublishedApi
    internal fun makeFlowFinalization(record: T, flow: ProceduralFlow<*>):T{
        flow.complete(output = true)
        removeDataHandler(LogMessage::class)
        dropCollector(true)
        if (verbosity == Verbosity.Debug) {
            StateSnapshot(this).output("Before record  $record logged")
        }
        logData(record, noOutput = true)
        if (verbosity == Verbosity.Debug) {
            StateSnapshot(this).output("After record  $record logged")
        }
        return record
    }

    fun <H: Component> newProceduralFlow(
        record: StructuredLoggable,
    ): ProceduralFlow<H> {
        val procedural =  ProceduralFlow.toProceduralRecord(record)
        val casted = record.castOrThrow<T>(messageTypeToken.kClass)
        activeRecord = casted
        val flow = ProceduralFlow(this, procedural)
        useHandler(flow)
        return flow.castOrThrow<ProceduralFlow<H>>()
    }

    fun finalizeFlow(flow: ProceduralFlow<*>):T{
      return  activeRecord?.let {
          makeFlowFinalization(it, flow)
        }?:run {
            throw IllegalStateException("No activeRecord")
        }
    }

    fun finalizeHandler(handler: LogHandler):T?{
        return when(handler){
            is ProceduralFlow<*> ->{
                finalizeFlow(handler)
            }
            else -> {
                handler.completionSignal.trigger(handler)
                null
            }
        }
    }

    fun createProceduralFlow(data: T): ProceduralFlow<H>{
        val record = ProceduralFlow.toProceduralRecord(data)
        val flow = ProceduralFlow(this,  record)
        useHandler(flow)
        activeRecord = data
        return flow
    }

    inline fun <R> proceduralScope(
        record: T,
        crossinline block: ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R {
        val flow = createProceduralFlow(record)
        val result = block.invoke(flow, flow.proceduralRecord)
        makeFlowFinalization(record, flow)
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
        makeFlowFinalization(record, flow)
        return result
    }

    fun useProcedural(proceduralFlow: ProceduralFlow<*>): LogHandler? =
        useHandler(proceduralFlow)

    override fun outputOrNot(data: Loggable) {
        if (data.topic >= verbosity.minTopic) {
            data.output()
        }
    }

    override fun handleUnAssigned(message: Loggable) {
        val message = "$loggerName Unable to process loggable message of ${message::class}"
        message.output(Colour.Yellow)
    }

    inline fun <reified LH: LogHandler> getHandlerOf(): LH?{
       val handler =  this.logForwarder.getHandler(LH::class)
        return handler?.castOrThrow<LH>()
    }

    inline fun <reified LH: LogHandler> getHandlerOf(throwing: Throwing): LH{
        return  logForwarder.getHandler(LH::class).castOrThrow<LH>()
    }


    fun forwardOutputTo(logProcessor: LogProcessor<out Component, out StructuredLoggable>){
        val myDataClass = messageTypeToken.kClass
        //logProcessor.logForwarder.dataHandlers.keys.firstOrNull{ myDataClass.isSubclassOf(it) }
        val handlerFound = logProcessor.logForwarder.getHandlerFor(myDataClass)
        if(handlerFound != null){
            useHandler(handlerFound)
        }else{
            notify(outputImmediately = true, "forwardOutputTo", "Processed data class is ${myDataClass}. No key found")
        }
    }
}



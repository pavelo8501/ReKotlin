package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.Notification
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.processors.LoggerContext
import po.misc.functions.LambdaType
import po.misc.types.token.TypeToken
import kotlin.reflect.full.isSubclassOf


class LogProcessor <H: Component, LR: Loggable>(
    override val host: H,
    val typeToken: TypeToken<LR>,
    val provider: ((Loggable)->LR)? = null
): LogProcessorBase<LR>(host), LoggerContext {

    private val abnormalSubject = "Abnormal State"

    private val dataProcessSubject = "Data Processing"
    private val impossibleToProcess : (String, String)-> String = {method, reason,->
        "Unable to run $method. Reason: $reason"
    }

    private fun fallbackRecord():Loggable{
       return Notification(host, NotificationTopic.Warning, abnormalSubject, "Using Loggable instead of LR:Loggable as a last chance")
    }

    @Suppress("Unchecked_cast")
    val activeOrNew: LR get() {
        return activeRecord?:run {
            val notification = fallbackRecord()
            provider?.invoke(notification) ?:notification as LR
        }
    }

    /**
     * Runs a procedural log scope where each operation inside [block] is captured
     * as a structured sequence of [ProceduralEntry] items within the provided [record].
     *
     * When the scope completes, [record] is finalized and logged as [LR].
     *
     * @param record the procedural record to populate
     * @param block the logic whose steps are recorded
     * @return the result returned by [block]
     */
    fun <PR: StructuredLoggable, R> logScope(
        record: PR,
        block: ProceduralFlow<H, PR>.()-> R
    ):R {
        val flow = ProceduralFlow(host, record.subject, record)
        val result = flow.block()

        @Suppress("Unchecked_Cast")
        if(record::class.isSubclassOf(typeToken.kClass)){
            if(record is ProceduralRecord){
                record.finalizeRecord()
            }
            logData(record as LR)
        }else{
            host.warn(abnormalSubject, "Downcast of record<PR:ProceduralEntryReady> to ${typeToken.typeName} is impossible. Record can not be logged")
        }
        return result
    }

   suspend fun <PR: StructuredLoggable, R> logScope(
        suspending: LambdaType.Suspended,
        record: PR,
        block: suspend ProceduralFlow<H, PR>.()-> R
    ):R{
       val flow = ProceduralFlow(host, record.subject, record)
       val result = block.invoke(flow)
       @Suppress("Unchecked_Cast")
       if(record::class.isSubclassOf(typeToken.kClass)){
           if(record is ProceduralRecord){
               record.finalizeRecord()
           }
           logData(record as LR)
       }else{
           host.warn(abnormalSubject, "Downcast of record<PR:ProceduralEntryReady> to ${typeToken.typeName} is impossible. Record can not be logged")
       }
       return result
    }

    fun <SL, R> logScope(
        subject: String,
        block: ProceduralFlow<H, SL>.()-> R
    ):R  where  SL: StructuredLoggable{

       val loggable = host.loggable(NotificationTopic.Info, subject, "Start")
       return provider?.let {
            val procedural = it.invoke(loggable)

           @Suppress("Unchecked_Cast")
           val flow = ProceduralFlow(host, procedural.subject, procedural as SL)
           val blockResult = flow.block()

            if (procedural::class.isSubclassOf(StructuredLoggable::class)) {
                if (procedural is ProceduralRecord) {
                    procedural.finalizeRecord()
                }
                logData(procedural as LR)
            } else {
                host.warn(abnormalSubject, "Not as subclass of ")
            }
           blockResult
        }?:run {
            val fallback = fallbackRecord()
            @Suppress("Unchecked_Cast")
            val flow = ProceduralFlow(host, fallback.subject, fallback as SL)
            flow.block()
        }
    }

    fun <R> withProcedural(
        record: ProceduralRecord,
        block: H.()->R
    ): R {
        val thisHost = host
        val proceduralList = mutableListOf<ProceduralEntry>()
        collectData(keepData = false){loggable->
            val procedural = loggable.toProceduralEntry()
            proceduralList.add(procedural)
        }
        val result = block.invoke(thisHost)
        val proceduralResult =  ProceduralEntry.toEntryResult(result)
        proceduralList.forEach {emittedProcedural->
            emittedProcedural.provideResult(proceduralResult)
            record.procedural.lastOrNull()?.let {
                it.subEntries.add(emittedProcedural)
            }?:run {
                record.registerRecord(emittedProcedural)
            }
        }
        dropCollector()
        return result
    }

    fun logRecord(loggable: Loggable){
        provider?.let {
            val record = it.invoke(loggable)
            logData(record)
        }?:run {
            host.warn(dataProcessSubject, impossibleToProcess("logRecord", "Provider for type<LR: Loggable> not defined"))
        }
    }

    override fun outputOrNot(data: LR) {
        if (data.topic >= verbosity.minTopic) {
            data.output()
        }
    }
}



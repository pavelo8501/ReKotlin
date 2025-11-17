package po.misc.data.logging.procedural

import po.misc.collections.asList
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.log_subject.InfoSubject
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.track
import po.misc.data.badges.Badge
import po.misc.data.logging.log_subject.WarningSubject
import po.misc.functions.Suspending
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KClass


/**
 * A procedural execution context that allows a [TraceableContext] host to log structured,
 * step-based actions in sequence.
 *
 * Each step (via [proceduralStep]) automatically creates and registers a [ProceduralEntry],
 * capturing the result of the block (success, failure, or exception).
 *
 * Nested procedural flows can be attached to a parent [ProceduralRecord] using
 * [po.misc.data.logging.processor.LogProcessor.withProcedural].
 *
 * Example:
 * ```kotlin
 * processor.logScope(record) {
 *     proceduralStep("Load Data") { loadData() }
 *     proceduralStep("Validate Results") { validateData() }
 * }
 * ```
 *
 * @param H the host context performing the procedural flow
 * @param LR the procedural record type used for aggregation
 */
class ProceduralFlow<H: Component>(
    var logProcessor: LogProcessor<H, out StructuredLoggable>,
    override val proceduralRecord: ProceduralRecord
): TraceableContext, ProcFlowHandler<H>, LogHandler {

    override val host: H get() = logProcessor.host
    override val baseClassHandled: KClass<StructuredLoggable> = StructuredLoggable::class
    val name: String get() =  "ProceduralFlow on ${logProcessor.name}"

    private var maxLineReg: Int = 4
        set(value) {
            if (value > field) {
                field = value
            }
        }

    private val resultSubject: String = "Result handling"

    val trackMessage : (String, StructuredLoggable) -> String = {method, data->
        "$name -> $method(${data})"
    }

    @PublishedApi
    internal inline fun <R> processStep(
        stepName: String,
        badge: Badge?,
        tolerance: Collection<StepTolerance> = emptyList(),
        crossinline block: H.(ProcFlowHandler<H>) -> R
    ): Pair<StepResult, R> {
        val entry: ProceduralEntry =  createEntry(stepName, badge)
        proceduralRecord.registerEntry(entry)
        val result =  block.invoke(host, this)
        val stepResult =  toStepResult(entry, result, tolerance)
        entry.stepResult = stepResult
        return Pair(stepResult, result)
    }


  @PublishedApi
    internal suspend inline fun <R> processStepSuspending(
        stepName: String,
        badge: Badge?,
        tolerance: Collection<StepTolerance>,
        crossinline block: suspend H.(ProcFlowHandler<H>) -> R
    ): Pair<StepResult, R> {
        val entry: ProceduralEntry =  createEntry(stepName, badge)
        val result =  block.invoke(host, this)
        val stepResult =  toStepResult(entry, result, tolerance)
        entry.stepResult = stepResult
        return Pair(stepResult, result)
    }

    inline fun <R> step(
        stepName: String,
        badge: Badge? = null,
        vararg tolerance: StepTolerance,
        crossinline block: H.(ProcFlowHandler<H>) -> R
    ): R  = processStep(stepName, badge, tolerance.toList(), block = block).second


    inline fun <H: Component, SL: StructuredLoggable,  R> proceduralPage(
        logProcessor: LogProcessor<H, SL>,
        message: LogMessage,
       crossinline  block: ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R  {

        logProcessor.collectData(keepData = false, setGeneralMute = true,  ::processData)
        val procedural = createRecord(message)
        procedural.indentSize = proceduralRecord.indentSize + 1
        proceduralRecord.registerEntry(procedural)
        val result =  logProcessor.proceduralScope(procedural,  block)

        val resultString = toProceduralResult(result)
        proceduralRecord.resultPostfix = resultString
        message.track(this, "proceduralPage")
        logProcessor.dropCollector(dropGeneralMute = true)
        return result
    }


    internal fun <H: Component, SL: StructuredLoggable,  R> proceduralPage(
        logProcessor: LogProcessor<H, SL>,
        proceduralRecord: ProceduralRecord,
        block: ProceduralFlow<H>.(ProceduralRecord) -> R
    ): R  {
        logProcessor.collectData(keepData = false, setGeneralMute = true,  ::processData)
        val result =  logProcessor.proceduralScope(proceduralRecord,  block)
        val resultString = toProceduralResult(result)
        proceduralRecord.resultPostfix = resultString
        logProcessor.dropCollector(dropGeneralMute = true)
        proceduralRecord.logMessage.track(this, "proceduralPage(ProceduralRecord)")
        return result
    }

    suspend inline fun <R> step(
        suspending: Suspending,
        stepName: String,
        badge: Badge,
        vararg tolerance: StepTolerance,
        crossinline block: suspend H.(ProcFlowHandler<H>) -> R
    ): R = processStepSuspending(stepName, badge,  tolerance.toList(), block).second


    override fun processData(data: StructuredLoggable) {
        data.track(this, "processData")
        proceduralRecord.records.add(data)
    }

    fun complete(output: Boolean = true): LogMessage {
        proceduralRecord.logMessage.track(this, "complete")
        proceduralRecord.calculateResult()
        proceduralRecord.extractMessage()
        if(output){
            proceduralRecord.outputRecord()
        }
        val sourceMessage = proceduralRecord.extractMessage()
        return sourceMessage
    }

    companion object {
        private fun containsWarnings(entry: ProceduralEntry): List<Loggable>?{
            val filtered =  entry.logRecords.filter { it.topic == NotificationTopic.Warning }
            return filtered.ifEmpty {
                null
            }
        }

        private fun toEntry(text: String, loggable: StructuredLoggable,  badge: Badge? = null): ProceduralEntry{
            val entry =  ProceduralEntry(badge?: Badge.Process, text)
            entry.add(loggable)
            return entry
        }

        fun createEntry(loggable: StructuredLoggable) : ProceduralEntry{
           return when(loggable.topic){
                NotificationTopic.Warning ->{
                  val entry =  ProceduralEntry(WarningSubject.Warning, loggable.subject)
                   entry.stepResult =  StepResult.Warning(loggable.asList())
                   entry
                }
                else ->{
                    ProceduralEntry(Badge.Process, loggable.subject)
                }
            }
        }

        fun createEntry(text: String, badge: Badge? = null): ProceduralEntry{
            val badge = badge?: Badge.Process
            return ProceduralEntry(badge, text)
        }

        fun createRecord(message: StructuredLoggable): ProceduralRecord {
            return ProceduralRecord(message)
        }

        fun createRecord(parentProcedural: ProceduralRecord,   message: LogMessage): ProceduralRecord {
            val procedural =  ProceduralRecord(message)
            parentProcedural.registerRecord(procedural)
            return procedural
        }

        fun <R> toProceduralResult(
            result: R,
        ): String {
            return when (result) {
                is List<*> -> {
                    result.firstOrNull()?.let {
                        "[ ${result.size} of ${it::class.simpleOrAnon} ]"
                    } ?: run {
                        "[ 0 items ]"
                    }
                }
                is Boolean -> "[ ${result.toString()} ]"
                else -> ""
            }
        }


        fun <R> toStepResult(
            entry: ProceduralEntry,
            result: R,
            tolerances: Collection<StepTolerance> = emptyList()
        ): StepResult{
            val warnings = containsWarnings(entry)
            return when (result) {
                is List<*> -> {
                    val allowed = result.isNotEmpty() || StepTolerance.ALLOW_EMPTY_LIST in tolerances
                    if (allowed) {
                        if(warnings != null){
                            StepResult.Warning(warnings)
                        }else{
                            StepResult.OK()
                        }
                    }
                    else StepResult.Fail()
                }
                is Boolean -> {
                    val allowed = result || StepTolerance.ALLOW_FALSE in tolerances
                    if (allowed) {
                        if(warnings != null){
                            StepResult.Warning(warnings)
                        }else{
                            StepResult.OK()
                        }
                    }
                    else StepResult.Fail()
                }
                null -> {
                    if (StepTolerance.ALLOW_NULL in tolerances) {
                        if(warnings != null){
                            StepResult.Warning(warnings)
                        }else{
                            StepResult.OK()
                        }
                    }
                    else StepResult.Fail()
                }
                else -> {
                    if(warnings != null){
                        StepResult.Warning(warnings)
                    }else{
                        StepResult.OK()
                    }
                }
            }
        }

        fun <R> toStepResult(
            entry: ProceduralEntry,
            result: R, vararg tolerances: StepTolerance
        ): StepResult = toStepResult(entry, result, tolerances.toList())
    }
}

sealed interface ProcFlowHandler<H: Component>{
    val host: H
    val proceduralRecord: ProceduralRecord

    fun infoMsg(subject: String, text: String): LogMessage = host.infoMsg(subject, text)
    fun infoMsg(
        infoSubject: InfoSubject,
        text: String? = null
    ): LogMessage = host.infoMsg(infoSubject, text)

}
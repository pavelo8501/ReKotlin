package po.misc.data.logging.procedural

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.SignalOptions
import po.misc.callbacks.signal.signalOf
import po.misc.collections.asList
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.track
import po.misc.data.badges.Badge
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.log_subject.WarningSubject
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.LogProcessor
import po.misc.functions.Suspending
import po.misc.types.k_class.simpleOrAnon
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
 */
class ProceduralFlow<H: Component>(
    var logProcessor: LogProcessor<H, out StructuredLoggable>,
    override val proceduralRecord: ProceduralRecord
): TraceableContext, ProcFlowHandler<H>, LogHandler{

    override val host: H get() = logProcessor.host
    override val targetClassHandled: KClass<out StructuredLoggable> = LogMessage::class

    val name: String get() =  "ProceduralFlow on ${logProcessor.name}"

    override val completionSignal: Signal<LogHandler, Unit> = signalOf(SignalOptions("CompletionSignal", this))

    private var maxLineReg: Int = 4
        set(value) {
            if (value > field) {
                field = value
            }
        }

    private val resultSubject: String = "Result handling"

    var debug: Boolean = false

    val trackMessage : (String, StructuredLoggable) -> String = {method, data->
        "$name -> $method(${data})"
    }

    internal val templater = ProceduralTemplater(this)

    @PublishedApi
    internal inline fun <R> processStep(
        stepName: String,
        badge: Badge?,
        tolerance: Collection<StepTolerance> = emptyList(),
        crossinline block: H.(ProcFlowHandler<H>) -> R
    ): Pair<StepResult, R> {
        val entry: ProceduralEntry =  createEntry(proceduralRecord, stepName, badge)
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
        val entry: ProceduralEntry =  createEntry(proceduralRecord,  stepName, badge)
        val result =  block.invoke(host, this)
        val stepResult =  toStepResult(entry, result, tolerance)
        entry.stepResult = stepResult
        return Pair(stepResult, result)
    }

    fun findStep(
        stepName: String? = null
    ): ProceduralEntry? = proceduralRecord.findEntry(stepName)


    fun createStep(
        stepName: String,
        badge: Badge? = null
    ): ProceduralEntry {
        val entry: ProceduralEntry = createEntry(proceduralRecord, stepName, badge)
        proceduralRecord.addEntry(entry)
        return entry
    }

    fun <R> finalizeStep(
        entry: ProceduralEntry,
        result: R? = null,
        vararg tolerance: StepTolerance
    ): Boolean {
        val stepResult = toStepResult(entry, result)
        entry.stepResult = stepResult
        return true
    }

    fun <R> finalizeStep(
        name: String? = null,
        result: R? = null,
        vararg tolerance: StepTolerance
    ): Boolean {
        val found =  findStep(name)
        if(found != null){
            val stepResult = toStepResult(found, result)
            found.stepResult = stepResult
            return true
        }else{
            return false
        }
    }

    inline fun <R> step(
        stepName: String,
        badge: Badge? = null,
        vararg tolerance: StepTolerance,
        crossinline block: H.(ProcFlowHandler<H>) -> R
    ): R  = processStep(stepName, badge, tolerance.toList(), block = block).second

    suspend inline fun <R> step(
        suspending: Suspending,
        stepName: String,
        badge: Badge,
        vararg tolerance: StepTolerance,
        crossinline block: suspend H.(ProcFlowHandler<H>) -> R
    ): R = processStepSuspending(stepName, badge,  tolerance.toList(), block).second

    override fun processRecord(logRecord: StructuredLoggable) {
        logRecord.track(this, "processData")
        templater.processRecord(logRecord)
        //proceduralRecord.addRecord(logRecord)
    }

    fun complete(output: Boolean = true): LogMessage {
        proceduralRecord.logRecord.track(this, "complete")
        proceduralRecord.calculateResult()
        proceduralRecord.extractMessage()
        if(output){
            proceduralRecord.outputRecord()
        }
        val sourceMessage = proceduralRecord.extractMessage()
        completionSignal.trigger(this)
        return sourceMessage
    }

    companion object {

        fun createEntry(record: ProceduralRecord, structured: StructuredLoggable) : ProceduralEntry {

            return when (structured.topic) {
                NotificationTopic.Warning -> {
                    val resultWarning = StepResult.Warning(structured.asList())
                    ProceduralEntry(WarningSubject.Warning, structured.subject, resultWarning, record)
                }
                else -> ProceduralEntry(Badge.Process, structured.subject, record)
            }
        }
        fun createEntry(record: ProceduralRecord, text: String, badge: Badge? = null): ProceduralEntry{
            val badge = badge?: Badge.Process
            return ProceduralEntry(badge, text, record)
        }
        fun createEntry(
            parentProcedural: LoggableTemplate,
            record: StructuredLoggable,
            badge: Badge? = null
        ): ProceduralEntry{
            val entry = ProceduralEntry(record, parentProcedural, result = null, stepBadge =  badge)
            val result = toStepResult(entry, Unit)
            entry.stepResult = result
            return entry
        }

        fun toProceduralRecord(message: StructuredLoggable, topNode: Boolean = true): ProceduralRecord {
            val procedural =  ProceduralRecord(message, topNode)
            return procedural
        }
        fun createRecord(parentProcedural: ProceduralRecord, message: LogMessage): ProceduralRecord {
            val procedural =  ProceduralRecord(message,  topNode = false)
            parentProcedural.registerRecord(procedural)
            return procedural
        }

        fun <R> toProceduralResult(result: R): String {
            return when (result) {
                is List<*> -> {
                    result.firstOrNull()?.let {
                        "[ ${result.size} of ${it::class.simpleOrAnon} ]"
                    } ?: run {
                        "[ 0 items ]"
                    }
                }
                is Boolean -> "[ $result ]"
                else -> ""
            }
        }

        private fun containsWarnings(entry: ProceduralEntry): List<Loggable>?{
            val filtered =  entry.logRecords.filter { it.topic == NotificationTopic.Warning }
            return filtered.ifEmpty {
                null
            }
        }
        private fun boolResult(result: Boolean, tolerances: Collection<StepTolerance>, warnings: List<Loggable>?): StepResult{
            val allowed = result || StepTolerance.ALLOW_FALSE in tolerances
            return if (allowed) {
                if(warnings != null){
                    StepResult.Warning(warnings)
                }else{
                    StepResult.OK()
                }
            }
            else StepResult.Fail()
        }
        private fun nullResult(tolerances: Collection<StepTolerance>, warnings: List<Loggable>?): StepResult{
            return if(StepTolerance.ALLOW_NULL in tolerances) {
                if(warnings != null){
                    StepResult.Warning(warnings)
                }else{
                    StepResult.OK()
                }
            }
            else StepResult.Fail()
        }
        private fun listResult(result: List<*>, tolerances: Collection<StepTolerance>, warnings: List<Loggable>?):StepResult{
           val allowed = result.isNotEmpty() || StepTolerance.ALLOW_EMPTY_LIST in tolerances
           return if (allowed) {
                if(warnings != null){
                    StepResult.Warning(warnings)
                }else{
                    StepResult.OK()
                }
            }
            else StepResult.Fail()
        }

        fun <R> toStepResult(entry: ProceduralEntry, result: R, tolerances: Collection<StepTolerance>): StepResult {
            val warnings = containsWarnings(entry)
            return when (result) {
                is List<*> -> listResult(result, tolerances, warnings)
                is Boolean -> boolResult(result, tolerances, warnings)
                null -> nullResult(tolerances, warnings)
                else -> {
                    if(warnings != null) StepResult.Warning(warnings)
                    else StepResult.OK()
                }
            }
        }

        fun <R> toStepResult(entry: ProceduralEntry, result: R, vararg tolerances: StepTolerance): StepResult =
            toStepResult(entry, result, tolerances.toList())
    }
}

sealed interface ProcFlowHandler<H: TraceableContext>{
    val host: H
    val proceduralRecord: ProceduralRecord
}
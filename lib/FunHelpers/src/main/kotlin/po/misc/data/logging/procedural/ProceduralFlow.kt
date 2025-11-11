package po.misc.data.logging.procedural

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.StructuredLoggable
import po.misc.functions.LambdaType


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
class ProceduralFlow<H: Component, LR: StructuredLoggable>(
    val host: H,
    val subject: String,
    val logRecord: LR
): TraceableContext{

    private var maxLineReg: Int = 4
        set(value) {
            if(value > field){
                field = value
            }
        }
    internal val entries = mutableListOf<ProceduralEntry>()

    private fun submitEntry(proceduralEntry: ProceduralEntry){
        logRecord.registerRecord(proceduralEntry)
        entries.add(proceduralEntry)
    }


    fun <T: TraceableContext, R: Any?> proceduralStep(
        stepName: String,
        vararg tolerance : StepTolerance,
        parameter: T,
        block: H.(T)->R
    ):R{
        var inBlockThrowable : Throwable? = null
        val tempSolutionForBadge = "[SYNC]"
        val record = ProceduralEntry(stepName, tempSolutionForBadge){
            maxLineReg
        }
        submitEntry(record)
        val blockResult =  try {
            block(host, parameter)
        }catch (th: Throwable){
            inBlockThrowable = th
            null
        }

        val entryResult = ProceduralEntry.toEntryResult(blockResult, tolerance.toList())
        record.provideResult(entryResult)
        maxLineReg = record.stepBadge.count() + record.stepName.count()
        return  inBlockThrowable?.let {
            throw  it
        }?:run {
            @Suppress("UNCHECKED_CAST")
            blockResult as R
        }
    }
   inline fun <reified T: TraceableContext, R: Any?> proceduralStep(
        stepName: String,
        parameter: T,
       noinline block: H.(T)->R
    ): R = proceduralStep(stepName, StepTolerance.STRICT, parameter =  parameter, block =  block)


    suspend fun <T: TraceableContext, R: Any?> proceduralStep(
        suspended: LambdaType.Suspended,
        stepName: String,
        vararg tolerance : StepTolerance,
        parameter: T,
        block: suspend H.(T)->R
    ):R{
        var inBlockThrowable : Throwable? = null
        val tempSolutionForBadge = "[SYNC]"
        val record = ProceduralEntry(stepName, tempSolutionForBadge){
            maxLineReg
        }
        submitEntry(record)
        val blockResult =  try {
            block.invoke(host, parameter)
        }catch (th: Throwable){
            inBlockThrowable = th
            null
        }

        val entryResult = ProceduralEntry.toEntryResult(blockResult, tolerance.toList())
        record.provideResult(entryResult)
        maxLineReg = record.stepBadge.count() + record.stepName.count()
        return  inBlockThrowable?.let {
            throw  it
        }?:run {
            @Suppress("UNCHECKED_CAST")
            blockResult as R
        }
    }

    suspend inline fun <reified T: TraceableContext, R: Any?> proceduralStep(
        suspending: LambdaType.Suspended,
        stepName: String,
        parameter: T,
        noinline block: suspend H.(T)->R
    ): R = proceduralStep(suspending, stepName, StepTolerance.STRICT, parameter =  parameter, block =  block)



    fun <R: Any?> proceduralStep(
        stepName: String,
        vararg tolerance : StepTolerance,
        block: H.()->R
    ):R {
        var inBlockThrowable : Throwable? = null
        val tempSolutionForBadge = "[SYNC]"
        val record = ProceduralEntry(stepName, tempSolutionForBadge){
            maxLineReg
        }
        submitEntry(record)
        val blockResult =  try {
            block(host)
        }catch (th: Throwable){
            inBlockThrowable = th
            null
        }
        val entryResult = ProceduralEntry.toEntryResult(blockResult, tolerance.toList())
        record.provideResult(entryResult)
        maxLineReg = record.stepBadge.count() + record.stepName.count()

        return  inBlockThrowable?.let {
            throw  it
        }?:run {
            @Suppress("UNCHECKED_CAST")
            blockResult as R
        }
    }

    fun <R: Any?> proceduralStep(
        stepName: String,
        block: H.()->R
    ):R = proceduralStep(stepName, StepTolerance.STRICT, block =  block)


    suspend fun <R: Any?> proceduralStep(
        suspending: LambdaType.Suspended,
        stepName: String,
        vararg tolerance : StepTolerance,
        block: suspend H.()->R
    ):R {
        var inBlockThrowable : Throwable? = null
        val tempSolutionForBadge = "[SYNC]"
        val record = ProceduralEntry(stepName, tempSolutionForBadge){
            maxLineReg
        }
        submitEntry(record)
        val blockResult =  try {
            block.invoke(host)
        }catch (th: Throwable){
            inBlockThrowable = th
            null
        }
        val entryResult = ProceduralEntry.toEntryResult(blockResult, tolerance.toList())
        record.provideResult(entryResult)
        maxLineReg = record.stepBadge.count() + record.stepName.count()

        return  inBlockThrowable?.let {
            throw  it
        }?:run {
            @Suppress("UNCHECKED_CAST")
            blockResult as R
        }
    }

   suspend fun <R: Any?> proceduralStep(
        suspending: LambdaType.Suspended,
        stepName: String,
        block: suspend H.()->R
    ):R = proceduralStep(suspending, stepName, StepTolerance.STRICT, block =  block)

}
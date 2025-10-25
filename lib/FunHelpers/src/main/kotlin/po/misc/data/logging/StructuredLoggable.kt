package po.misc.data.logging

import po.misc.data.logging.procedural.ProceduralEntry

/**
 * Represents a [Loggable] entity capable of holding structured,
 * step-based log records that describe a multi-phase or procedural operation.
 *
 * Implementations can collect [ProceduralEntry] items to describe
 * individual steps, outcomes, or nested phases of a larger process.
 *
 * This interface bridges the gap between simple logging events
 * ([Loggable]) and more complex, structured logging flows, such as those
 * managed by [po.misc.data.logging.processor.LogProcessor.logScope] or [po.misc.data.logging.procedural.ProceduralFlow].
 *
 * Typical use cases include configuration parsing, batch processing,
 * or multi-step transaction flows where detailed procedural context is required.
 *
 * Implementations are expected to:
 * - Represent a higher-level log entity (e.g. operation, job, or flow)
 * - Store related procedural steps via [registerRecord]
 * - Optionally define a finalization or summary step (e.g. success/failure)
 *
 * @see Loggable
 * @see po.misc.data.logging.procedural.ProceduralRecord
 * @see ProceduralEntry
 * @see po.misc.data.logging.procedural.ProceduralFlow
 */
interface StructuredLoggable: Loggable {

    /**
     * Registers a procedural step or sub-entry within this structured log.
     * @param record the procedural entry describing an individual step
     *               or nested flow that occurred during the operation
     */
    fun registerRecord(record: ProceduralEntry)
}
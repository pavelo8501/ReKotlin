package po.misc.data.logging

import po.misc.context.component.Component


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
 * @see LogProcessor for log collection and dispatch.
 * @see Component for identity and verbosity management.
 */
interface LogProvider<LR: Loggable>: Component



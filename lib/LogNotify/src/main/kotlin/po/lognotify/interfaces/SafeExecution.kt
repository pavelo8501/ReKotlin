package po.lognotify.interfaces

import po.misc.context.ManagedContext

/**
 * Public contract for user-defined or third-party logic that can be executed
 * in a controlled environment with optional logging, timing, and exception handling.
 *
 * Implement this interface when contributing executable blocks or integrations
 * to systems that support structured execution (e.g., pipelines, task schedulers,
 * plugin engines).
 *
 * Unlike [po.lognotify.execution.ControlledExecution], this interface is designed to be stable and safe
 * for external usage without depending on internal framework guarantees.
 */
interface SafeExecution : ManagedContext {



}
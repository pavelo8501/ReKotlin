package po.misc.functions.hooks

import po.misc.context.CTX


/**
 * Defines a set of lifecycle hooks for delegation and reactive value tracking.
 *
 * This interface allows registration of custom logic to be executed at different
 * stages of a value's lifecycle, from the moment a provider context is set, through
 * resolution, changes, and eventual disposal.
 *
 * @param S the context type used during resolution or disposal.
 * @param V the type of the resolved value.
 */
interface BasicHooks<S: CTX, V: Any?>{

    /**
     * Registers a callback to be invoked when the provider context is first set.
     * @param onSet the function to invoke with the provided context.
     */
    fun onProviderSet(onSet: (S) -> Unit)

    /**
     * Registers a callback to be executed right before the value is resolved.
     * @param beforeResolve the function to invoke with the current context before resolution.
     */
    fun onBeforeResolve(beforeResolve: (S) -> Unit)

    /**
     * Registers a callback to be invoked after the value has been resolved.
     * @param onResolved the function to invoke with the resolved value.
     */
    fun onResolved(onResolved: (V) -> Unit)

    /**
     * Registers a callback to be executed with the resolved value using receiver-style access.
     * This is a convenient shorthand for executing logic directly on the value once it's resolved.
     * @param withValue the extension block to run with the resolved value as receiver.
     */
    fun withResolvedValue(withValue: V.() -> Unit)

    /**
     * Registers a callback to be invoked whenever the resolved value changes.
     * @param onChange the function to invoke with the previous and new value.
     */
    fun onChange(onChange: (V?, V) -> Unit)

    /**
     * Registers a callback to be executed when the context is being disposed.
     * @param onDispose the function to invoke with the context being disposed.
     */
    fun onDispose(onDispose: (S) -> Unit)
}

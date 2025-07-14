package po.misc.functions.hooks

import po.misc.functions.interfaces.FunctionalClass
import po.misc.interfaces.CTX



/**
 * Interface representing a reactive component that supports lifecycle hooks
 * around value provisioning and resolution.
 *
 * @param S The source type providing contextual information (must implement [CTX]).
 * @param V The type of the resolved value managed by this component.
 */
interface ReactiveComponent<S: CTX, V: Any> : FunctionalClass<V> {

    /**
     * Hook manager holding callbacks for various lifecycle events.
     */
    val hooks: ReactiveHooks<S, V>

    /**
     * Triggers the onChange hook manually.
     *
     * @param old The previous value.
     * @param new The new value to be reported.
     */
    fun notifyChanged(old: V?, new: V) {
        hooks.fireChange(old, new)
    }

    /**
     * Disposes the component and optionally fires dispose hooks.
     *
     * @param withHooks Whether to execute [onDispose] and clear hooks (default: true).
     */
    fun disposeHooks() {
        hooks.disposeHooks()
    }
}
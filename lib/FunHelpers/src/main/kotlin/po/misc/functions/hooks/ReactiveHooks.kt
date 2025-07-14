package po.misc.functions.hooks

import po.misc.interfaces.CTX


/**
 * Hook manager for tracking and reacting to state changes in a [ReactiveComponent].
 *
 * @param S The component type that owns this hook manager.
 * @param V The value type being managed by the component.
 * @property source The component instance that owns this hook set.
 */
class ReactiveHooks<S: CTX, V: Any>(private val source: S) {

    private var onProviderSetHook: ((S) -> Unit)? = null
    /**
     * Registers a callback that is invoked when the lambda provider is set.
     */
    fun onProviderSet(block: (S) -> Unit) {
        onProviderSetHook = block
    }
    internal fun fireProviderSet() = onProviderSetHook?.invoke(source)


    private var onBeforeResolveHook: ((S) -> Unit)? = null
    /**
     * Registers a callback to be called before resolution begins.
     */
    fun onBeforeResolve(block: (S) -> Unit){
        onBeforeResolveHook = block
    }
    internal fun fireBeforeResolve() = onBeforeResolveHook?.invoke(source)


    private var onResolvedHook: ((V) -> Unit)? = null
    /**
     * Registers a callback to be called after resolution completes.
     */
    fun onResolved(block: (V) -> Unit){
        onResolvedHook = block
    }
    private var withResolvedValueHook: (V.() -> Unit)? = null
    /**
     * Registers a lambda-style callback using receiver syntax, called with the resolved value.
     */
    fun withResolvedValue(block: V.() -> Unit){
        withResolvedValueHook = block
    }
    internal fun fireResolved(result: V) {
        onResolvedHook?.invoke(result)
        withResolvedValueHook?.invoke(result)
    }


    private var onChangeHook: ((old: V?, new: V) -> Unit)? = null
    /**
     * Registers a callback to be called whenever the value changes.
     *
     * @param block A function receiving the old and new values.
     */
    fun onChange(block: (V?, V) -> Unit){
        onChangeHook = block
    }
    internal fun fireChange(old: V?, new: V) = onChangeHook?.invoke(old, new)

    private var onDisposeHook: ((S) -> Unit)? = null
    /**
     * Registers a callback to be called when the component is disposed.
     */
    fun onDispose(block: (S) -> Unit){
        onDisposeHook = block
    }
    internal fun fireDispose() {
        onDisposeHook?.invoke(source)
    }

    /**
     * Clears all lifecycle hooks except [onDispose].
     */
    private fun clearHookBindings(){
        onProviderSetHook = null
        onBeforeResolveHook = null
        onResolvedHook = null
        onChangeHook = null
    }

    /**
     * Disposes all hooks and calls the dispose callback if registered.
     */
    fun disposeHooks(){
        clearHookBindings()
        fireDispose()
        onDisposeHook = null
    }

}
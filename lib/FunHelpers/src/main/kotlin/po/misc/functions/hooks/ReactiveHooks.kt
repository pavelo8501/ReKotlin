package po.misc.functions.hooks

import po.misc.context.CTX


/**
 * Hook manager for tracking and reacting to state changes in a [ReactiveComponent].
 * @param S The component type that owns this hook manager.
 * @param V The value type being managed by the component.
 * @property source The component instance that owns this hook set.
 */
class ReactiveHooks<S: CTX, V: Any?>(private var source: S? = null): BasicHooks<S, V> {

    val isInitialized: Boolean get() = source != null

    private var onProviderSetHook: ((S) -> Unit)? = null
    override fun onProviderSet(onSet: (S) -> Unit) {
        onProviderSetHook = onSet
    }
    internal fun fireProviderSet() {
        source?.let { onProviderSetHook?.invoke(it) }
    }

    private var onBeforeResolveHook: ((S) -> Unit)? = null
    override fun onBeforeResolve(beforeResolve: (S) -> Unit){
        onBeforeResolveHook = beforeResolve
    }
    internal fun fireBeforeResolve(){
        source?.let { onBeforeResolveHook?.invoke(it) }
    }

    private var onResolvedHook: ((V) -> Unit)? = null
    override fun onResolved(onResolved: (V) -> Unit){
        onResolvedHook = onResolved
    }
    private var withResolvedValueHook: (V.() -> Unit)? = null
    override fun withResolvedValue(withValue: V.() -> Unit){
        withResolvedValueHook = withValue
    }
    internal fun fireResolved(result: V) {
        onResolvedHook?.invoke(result)
        withResolvedValueHook?.invoke(result)
    }

    private var onChangeHook: ((old: V?, new: V) -> Unit)? = null
    override fun onChange(onChange: (V?, V) -> Unit){
        onChangeHook = onChange
    }
    internal fun fireChange(old: V?, new: V) = onChangeHook?.invoke(old, new)

    private var onDisposeHook: ((S) -> Unit)? = null
    override fun onDispose(onDispose: (S) -> Unit){
        onDisposeHook = onDispose
    }
    internal fun fireDispose() {
        source?.let { onDisposeHook?.invoke(it) }
    }

    private fun clearHookBindings(){
        onProviderSetHook = null
        onBeforeResolveHook = null
        onResolvedHook = null
        onChangeHook = null
    }

    fun initialize(source: S){
        this.source = source
    }

    fun disposeHooks(){
        clearHookBindings()
        fireDispose()
        onDisposeHook = null
    }

}
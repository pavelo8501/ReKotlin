package po.misc.functions.hooks


class ReactiveHooks<T : Any> {

    private var onInitHook: (() -> Unit)? = null
    private var onResolveHook: ((T) -> Unit)? = null
    private var onChangeHook: ((old: T, new: T) -> Unit)? = null
    private var onInvalidateHook: (() -> Unit)? = null
    private var onDisposeHook: (() -> Unit)? = null

    fun onInit(block: () -> Unit) = apply { onInitHook = block }
    fun onResolve(block: (T) -> Unit) = apply { onResolveHook = block }
    fun onChange(block: (T, T) -> Unit) = apply { onChangeHook = block }
    fun onInvalidate(block: () -> Unit) = apply { onInvalidateHook = block }
    fun onDispose(block: () -> Unit) = apply { onDisposeHook = block }

    internal fun fireInit() = onInitHook?.invoke()
    internal fun fireResolve(value: T) = onResolveHook?.invoke(value)
    internal fun fireChange(old: T, new: T) = onChangeHook?.invoke(old, new)
    internal fun fireInvalidate() = onInvalidateHook?.invoke()
    internal fun fireDispose() = onDisposeHook?.invoke()
}
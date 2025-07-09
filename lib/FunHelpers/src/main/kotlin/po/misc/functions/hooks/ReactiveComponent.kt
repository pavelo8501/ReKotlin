package po.misc.functions.hooks


import po.misc.functions.interfaces.FunctionalClass

interface ReactiveComponent<T : Any> : FunctionalClass<T> {

    val hooks: ReactiveHooks<T>

    /** Trigger hook manually (e.g. when external state changes) */
    fun notifyChanged(old: T, new: T) {
        hooks.fireChange(old, new)
    }

    fun dispose() {
        hooks.fireDispose()
    }
}
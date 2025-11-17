package po.misc.functions.hooks

import po.misc.context.CTX
import po.misc.functions.containers.LambdaUnit


interface Change<V1, V2>{
    val oldValue:V1
    val newValue:V2
}

/**
 * Contract for registering generic lifecycle hooks for any type [T] or value [V].
 *
 * This interface allows subscribing to events like initialization, change, and disposal
 * for components without needing type-specific implementations.
 */
interface DataHooksContract<T: Any, V: Any>{
    /**
     * Registers a hook that is triggered **before** the initialization of [T].
     * @param onBeforeInitialized A [LambdaUnit] to execute with the uninitialized context [T].
     */
    fun onBeforeInitialized(onBeforeInitialized: LambdaUnit<T, Unit>)

    /**
     * Registers a hook that is triggered **after** the successful initialization of [T].
     * @param onInitialized A [LambdaUnit] to execute with the initialized context [T].
     */
    fun  onInitialized(onInitialized: LambdaUnit<T, Unit>)

    /**
     * Registers a hook that is triggered when a value of type [V] is changed.
     * @param onChanged A [LambdaUnit] that receives a [Change] holding the old and new values.
     */
    fun  onChanged(onChanged:LambdaUnit<Change<V?, V>, Unit>)

    /**
     * Registers a hook that is triggered when [T] is disposed.
     * @param onDisposed A [LambdaUnit] to execute during the disposal phase.
     */
    fun  onDisposed(onDisposed: LambdaUnit<T, Unit>)
}


/**
 * Typed contract for managing lifecycle hooks for a specific context [T] and value type [V].
 * This interface provides strongly-typed lifecycle event listeners such as:
 * initialization, change events, and disposal callbacks.
 */
interface DataHooks<T: Any, V: Any>{
    /**
     * Called before [T] is initialized.
     * @param onBeforeInitialized A lambda executed with the uninitialized context [T].
     */
    fun onBeforeInitialized(onBeforeInitialized: (T) -> Unit)

    /**
     * Called after [T] is initialized.
     * @param onInitialized A lambda executed with the initialized context [T].
     */
    fun onInitialized(onInitialized: (T) -> Unit)

    /**
     * Called when the observed value changes.
     * @param onChanged A lambda executed with a [Change] holding old and new values.
     */
    fun onChanged(onChanged: (Change<V?, V>) -> Unit)

    /**
     * Called when [T] is disposed.
     * @param onDisposed A lambda executed with the disposed context [T].
     */
    fun onDisposed(onDisposed: (T) -> Unit)
}

internal interface ResponsiveData<T: Any, V: Any>: DataHooks<T, V>{
    fun triggerBeforeInitialized()
    fun triggerInitialized()
    fun triggerChanged(change:Change<V?, V>)
    fun triggerDisposed()
}

package po.misc.containers.lazy

import po.misc.containers.BackingContainerBase
import po.misc.types.token.TypeToken


/**
 * A write-once lazy value container with optional lambda-based initialization
 * and built-in value-available signaling.
 *
 * `LazyContainer<T>` behaves similarly to Kotlin's `lazy {}` but extends it
 * with the following features:
 *
 * ### ✅ Features
 * - **Write-once semantics**: a value can be provided only once unless
 *   the container is explicitly [reset].
 * - **Lazy lambda provisioning**: a value may be supplied via a lambda
 *   which is evaluated **once**, on the first call to [getValue], and
 *   its result is cached.
 * - **Eager provisioning**: a value may be provided directly via
 *   [provideValue(value)], which immediately locks the container against
 *   future writes.
 * - **Value-provided signaling**: listeners registered via
 *   `valueProvided(listener) { }` are invoked **exactly once** when the
 *   value becomes available.
 * - **Tracing and diagnostics**: accessing a value before initialization
 *   produces a structured trace and throws an [IllegalStateException].
 *
 * ### ✅ Edit Locking
 * When the final value becomes available—either through direct assignment
 * or lambda evaluation—the container becomes **locked for edit** via
 * [lockedForEdit], preventing accidental overwrites.
 *
 * ### ✅ Resetting
 * Calling [reset] clears:
 * - the stored value,
 * - the lazy provider,
 * - all pending `valueProvided` listeners,
 * allowing the container to be reused from an uninitialized state.
 *
 * @param T the type of value stored in the container
 * @param typeToken internal type information used for tracing/logging
 */
class LazyContainer<T: Any>(
    typeToken: TypeToken<T>
):BackingContainerBase<T>(typeToken){

    override var emissionType: EmissionType = EmissionType.EmmitOnce

    var lockedForEdit: Boolean = false
        private set

    override fun valueAvailable(value:T){
        lockedForEdit = true
    }

    /**
     * Immediately provides a concrete value for the container.
     *
     * The value is stored only if the container has not already been
     * initialized. Subsequent calls are ignored unless the container
     * has been [reset].
     *
     * @return this container for fluent chaining
     */
    fun provideValue(valueProvider: () -> T): LazyContainer<T> {
        super.provideValue(EmissionType.EmmitOnce, valueProvider)
        return this
    }

    /**
     * Provides a lazy initializer for the container.
     *
     * The lambda is evaluated **once**, on the first call to [getValue],
     * and its result becomes the permanent stored value unless the
     * container is later [reset].
     *
     * If any pending listeners were registered using `valueProvided`,
     * they are triggered once the lambda is evaluated.
     *
     * @return this container for fluent chaining
     */
    override fun provideValue(type : EmissionType, valueProvider:() ->T):LazyContainer<T> = provideValue(valueProvider)

    /**
     * Immediately provides a concrete value for the container.
     *
     * The value is stored only if the container has not already been
     * initialized. Subsequent calls are ignored unless the container
     * has been [reset].
     *
     * @return this container for fluent chaining
     */
    fun provideValue(newValue:T):LazyContainer<T>{
        super.provideValue(newValue, false)
        return this
    }
    override fun provideValue(newValue:T, allowOverwrite: Boolean):LazyContainer<T> = provideValue(newValue)

    companion object{
        inline operator fun <reified T: Any> invoke(
        ): LazyContainer<T>{
            return  LazyContainer(TypeToken.create<T>())
        }
    }
}
package po.misc.containers.backing

import po.misc.containers.BackingContainerBase
import po.misc.context.tracable.TraceableContext
import po.misc.functions.hooks.Change
import po.misc.types.token.TypeToken

/**
 * A container that manages and provides values of type [T], supporting value updates,
 * change notifications, and dependency tracking.
 *
 * [BackingContainer] allows you to:
 * - Store and provide values directly or via value providers
 * - Subscribe to value changes
 * - Track dependencies through [TraceableContext]
 * - Control emission behavior for value updates
 *
 * @param T The type of values managed by this container. Must be a non-nullable type.
 * @property emissionType Controls when value changes are emitted to subscribers.
 *                        Defaults to [EmissionType.EmmitAlways].
 *
 */
class BackingContainer<T: Any>(
    typeToken: TypeToken<T>
): BackingContainerBase<T>(typeToken) {

    override var emissionType: EmissionType = EmissionType.EmmitAlways

    override fun valueAvailable(value:T){

    }

    /**
     * Provides a value using a provider function that will be called when needed.
     * The provider function will be invoked whenever the value is requested and
     * the container needs to recompute it.
     *
     * @param type The emission type that controls how value changes are emitted
     * @param valueProvider A function that returns the value to provide
     * @return This container instance for method chaining
     *
     * @see EmissionType
     */
     override fun provideValue(type: EmissionType, valueProvider: () -> T): BackingContainer<T> {
         super.provideValue(type, valueProvider)
         return this
     }

    /**
     * Provides a concrete value to the container.
     * This immediately updates the container's value and notifies subscribers
     * according to the current [emissionType].
     *
     * @param newValue The new value to provide
     * @return This container instance for method chaining
     *
     */
    fun provideValue(newValue:T):BackingContainer<T>{
        super.provideValue(newValue, true)
        return this
    }

    /**
     * Registers a callback to be invoked whenever the container's value changes.
     * The callback receives a [Change] object containing both the old and new values.
     *
     * @param callback A function that handles value changes, receiving a [Change] object
     *                 with the previous and current values
     *
     */
    fun onValueChanged(callback: (Change<T?, T>) -> Unit) {
        changedHook.subscribe(callback)
    }

    override fun toString(): String = "BackingContainer<${typeToken.typeName}>"

    companion object {

        /**
         * Creates a new [BackingContainer] instance with optional initial value.
         * This factory method uses reified generics to infer the type token automatically.
         *
         * @param T The type of values the container will manage
         * @param initialValue Optional initial value for the container
         * @return A new [BackingContainer] instance
         *
         */
        inline operator fun <reified T : Any> invoke(
            initialValue: T? = null
        ): BackingContainer<T> {

            val container = BackingContainer(TypeToken.create<T>())
            if (initialValue != null) {
                container.provideValue(initialValue, true)
            }
            return container
        }
    }
}

package po.misc.containers

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.NullableProvider
import po.misc.functions.containers.Provider
import po.misc.types.Typed
import po.misc.types.getOrManaged


/**
 * A reusable container that holds a backing value with managed access and failure reporting.
 *
 * This class is typically used in systems where the backing value is not immediately available
 * but will be provided later via [provideSource]. Attempts to access [source] before the value
 * is set will trigger a failure via the [ManagedCallSitePayload].
 *
 * @param T the type of the backing value.
 * @property exPayload a payload used to report failures if the source is accessed prematurely.
 * @property typeData type metadata used for diagnostics or error reporting.
 * @constructor Creates a [BackingContainer] with optional initial [sourceBacking].
 */
open class BackingContainer<T: Any>(
    val exPayload: ManagedCallSitePayload,
    var typeData: Typed<T>? = null,
    private var sourceBacking:T? = null
){

    /**
     * Returns the current source value.
     * @throws ManagedException if the backing value is not available.
     */
    val source:T get(){
        return sourceBacking.getOrManaged(exPayload.valueFailure("sourceBacking", typeData.toString()))
    }

    /**
     * Indicates whether the source backing value has been provided.
     */
    val isSourceAvailable: Boolean get() = sourceBacking != null

    /**
     * Sets or replaces the backing source value.
     * @param source the value to assign as the backing source.
     */
    fun provideSource(source:T, type: Typed<T>){
        typeData = type
        sourceBacking = source
    }
}

class LazyBackingContainer<T: Any>(
    private var initialValue:T? = null
){
    var notifier : Notifier<T>? = null
    val isValueAvailable: Boolean get() = backingValue != null

    private var backingValue: T? = initialValue
    private var pendingCallback: ((T) -> Unit)? = null

    fun provideValue(value: T) {
        if (backingValue == null) {
            backingValue = value
            pendingCallback?.invoke(value)
            pendingCallback = null
        }
    }

    fun getValue(callback: (T) -> Unit) {
        val value = backingValue
        if (value != null) {
            callback(value)
        } else {
            pendingCallback = callback
        }
    }

    fun getValue():T?{
        return backingValue
    }

    fun reset() {
        backingValue = null
        pendingCallback = null
    }

}
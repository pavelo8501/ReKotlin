package po.misc.containers

import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.common.Fallback
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.NullableProvider
import po.misc.functions.containers.Provider
import po.misc.functions.subscribers.LambdaRegistry
import po.misc.functions.subscribers.TaggedLambdaRegistry
import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import kotlin.reflect.KClass


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
    val typeData: Typed<T>,
    private var valueBacking:T? = null
){

    /**
     * Returns the current source value.
     * @throws ManagedException if the backing value is not available.
     */
    val value:T? get() = valueBacking

    /**
     * Indicates whether the source backing value has been provided.
     */
    val isSourceAvailable: Boolean get() = valueBacking != null

    private var exceptionFallback: ExceptionFallback<T>? = null

    private fun getValueWithFallback(callingContext: Any):T{
        val currentValue = value
        if(currentValue != null){
            return currentValue
        }else{
            val payload = ManagedPayload("value is null","getValueWithFallback", callingContext)
            exceptionFallback?.exceptionProvider?.invoke(payload)
        }
        return  value.getOrManaged(typeData.kClass, callingContext)
    }

    /**
     * Sets or replaces the backing source value.
     * @param source the value to assign as the backing source.
     */
    fun provideSource(source:T){
        valueBacking = source
    }

    fun getValue(callingContext: Any):T{
       return  getValueWithFallback(callingContext)
    }

    fun provideFallback(fallback: ExceptionFallback<T>){
        exceptionFallback = fallback
    }

    companion object{
        inline fun <reified T: Any> create(initialValue:T? = null):BackingContainer<T>{
           return BackingContainer(TypeData.create<T>(), initialValue)
        }
    }
}

sealed class BackingContainerBase<T: Any>(){


    internal open var backingValue: T? = null

    protected abstract var pendingCallback: ((T) -> Unit)?



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

    fun getWithFallback(fallback: Fallback<T>):T{
        val value = backingValue
        if(value != null){
            return value
        }
        return fallback.initiateFallback()
    }

    fun reset() {
        backingValue = null
        pendingCallback = null
    }

}

class LazyBackingContainer<T: Any>(
    initialValue:T? = null
):BackingContainerBase<T>() {
    var notifier : Notifier<T>? = null
    val isValueAvailable: Boolean get() = backingValue != null


    override var backingValue: T? = initialValue
    override var pendingCallback: ((T) -> Unit)? = null

    fun provideValue(value: T) {
        if (backingValue == null) {
            backingValue = value
            pendingCallback?.invoke(value)
            pendingCallback = null
        }
    }
}

class TypedBackingContainer<V: Any>(
    initialValue:V? = null
):BackingContainerBase<V>() {

    val registry: LambdaRegistry<V> = LambdaRegistry()

    val isValueAvailable: Boolean get() = backingValue != null
    public override var backingValue: V? = initialValue
    override var pendingCallback: ((V) -> Unit)? = null

    fun provideValue(value: V) {
        println("provideValue ${value::class.simpleName}")
        registry.trigger(registry.defaultKey, value)
    }

    fun <V2: Any>  getUnsafeCasting(
        subscriber: CTX,
        kClass: KClass<V2>,
        callback : (V2) -> Unit
    ){
        println("getUnsafeCasting")
        val value = backingValue
        if(value != null){
            println("value != null invoking")
            val casted = value.safeCast(kClass)
            if(casted != null){
                callback.invoke(casted)
            }else{
                println("Cast to ${kClass.simpleName} failed wount invoke")
            }
        }else{
            println("Subscribing")
            registry.subscribe(subscriber.identity.numericId,  subscriber,  callback as (V) -> Unit)
        }
    }

}

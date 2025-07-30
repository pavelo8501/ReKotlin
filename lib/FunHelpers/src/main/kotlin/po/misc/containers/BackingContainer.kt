package po.misc.containers

import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.common.Fallback
import po.misc.functions.containers.Notifier
import po.misc.functions.subscribers.DefaultEvent
import po.misc.functions.subscribers.LambdaRegistry
import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import kotlin.reflect.KClass



sealed class BackingContainerBase<T: Any>(): LogEmitter{

    internal open var backingValue: T? = null

    /**
     * Returns the current source value.
     * @throws ManagedException if the backing value is not available.
     */
    val value:T? get() = backingValue

    /**
     * Indicates whether the source backing value has been provided.
     */
    val isValueAvailable: Boolean get() = backingValue != null

    abstract fun getValue(callingContext: Any):T


    fun <T2: Any> getWithFallback(fallback: Fallback<T2>):T{
        val value = backingValue
        if(value != null){
            return value
        }
        val fallbackValue = fallback.initiateFallback()

       val castedFallbackValue = when(this){
            is BackingContainer->{
                fallbackValue.castOrManaged(typeData.kClass, this)
            }
            else -> {
                TODO("else branch of getWithFallback not yet implemented")
            }
        }
        return castedFallbackValue
    }

    /**
     * Sets or replaces the backing source value.
     * @param value the value to assign as the backing source.
     */
    open fun provideValue(value:T){
        backingValue = value
    }

    fun reset() {
        backingValue = null
    }
}


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
): BackingContainerBase<T>() {

    private var exceptionFallback: ExceptionFallback? = null

    private fun getValueWithFallback(callingContext: Any): T {
        val currentValue = value
        if (currentValue != null) {
            return currentValue
        } else {
            val payload = ManagedPayload("value is null", "getValueWithFallback", callingContext)
            exceptionFallback?.exceptionProvider?.invoke(payload)
        }
        return value.getOrManaged(typeData.kClass, callingContext)
    }

    override fun getValue(callingContext: Any): T {
        return getValueWithFallback(callingContext)
    }

    fun provideFallback(fallback: ExceptionFallback) {
        exceptionFallback = fallback
    }

    companion object {

        inline fun <reified T : Any> create(initialValue: T? = null): BackingContainer<T> {
            val container = BackingContainer(TypeData.create<T>())
            if (initialValue != null) {
                container.provideValue(initialValue)
            }
            return container
        }
    }
}

inline fun <reified T: Any> backingContainerOf():BackingContainer<T>{
   return BackingContainer(TypeData.create<T>())
}



class LazyBackingContainer<T: Any>(
    initialValue:T? = null
):BackingContainerBase<T>() {

    var notifier : Notifier<T>? = null

    override var backingValue: T? = initialValue
    val registry: LambdaRegistry<T> = LambdaRegistry()


    override fun getValue(callingContext: Any): T {
        TODO("Not yet implemented")
    }

    fun <T2: Any>  requestValueCasting(
        subscriber: CTX,
        kClass: KClass<T2>,
        callback : (T2) -> Unit
    ){
        notify("requestValueCasting")
        val value = backingValue
        if(value != null){
            notify("value != null invoking")
            val casted = value.safeCast(kClass)
            if(casted != null){
                callback.invoke(casted)
            }else{
                notify("Cast to ${kClass.simpleName} failed wount invoke", SeverityLevel.WARNING)
            }
        }else{
            println("Subscribing")
            registry.subscribe(subscriber.identity.numericId,  subscriber,  callback as (T) -> Unit)
        }
    }

   override fun provideValue(value: T) {
       super.provideValue(value)
       registry.trigger(DefaultEvent.Default, value)
    }
}
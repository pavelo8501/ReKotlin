package po.misc.containers

import po.misc.context.CTX
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.common.Fallback
import po.misc.functions.hooks.Change
import po.misc.functions.hooks.ChangeHook
import po.misc.functions.hooks.models.ValueUpdate
import po.misc.functions.models.NotificationConfig
import po.misc.functions.registries.NotifierRegistry
import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import kotlin.reflect.KClass



sealed class BackingContainerBase<T: Any>(val typeData: Typed<T>, ): LogEmitter{

    protected open var backingValue: T? = null

    /**
     * Returns the current source value.
     * @throws ManagedException if the backing value is not available.
     */
    val value:T? get() = backingValue

    /**
     * Indicates whether the source backing value has been provided.
     */
    val isValueAvailable: Boolean get() = backingValue != null

    private var exceptionFallback: ExceptionFallback? = null
    private val registry = NotifierRegistry<T>()

    protected var changedHook: ChangeHook<T> = ChangeHook()

    private fun getValueWithFallback(callingContext: Any): T {
        val currentValue = value
        if (currentValue != null) {
            return currentValue
        } else {
            exceptionFallback?.exceptionProvider?.invoke()
        }
        return value.getOrManaged(typeData.kClass, callingContext)
    }

    private val valueNullMsg: (String)-> String = {name->
        "Context $name is trying to access uninitialized value in $this"
    }

    private fun notifyValueNull(callingContext: Any){
       val msg = when(callingContext){
            is CTX-> valueNullMsg(callingContext.identifiedByName)
            else ->  valueNullMsg(callingContext::class.simpleName.toString())
        }
        callingContext.notify(msg, SeverityLevel.WARNING)
    }

    fun provideFallback(fallback: ExceptionFallback) {
        exceptionFallback = fallback
    }

    fun getValue(callingContext: Any): T {
        value?:run {
            notifyValueNull(callingContext)
        }
        return value.getOrManaged(typeData.kClass, callingContext)
    }

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

    fun requestValue(subscriber: Any,  callback: (T)-> Unit){
        value?.let {
            callback.invoke(it)
        }?:run {
            val index = registry.subscriptionsCount + 1L
            registry.subscribe(subscriber, index, callback)
        }
    }

    /**
     * Sets or replaces the backing source value.
     * @param value the value to assign as the backing source.
     */
    open fun provideValue(value:T):BackingContainerBase<T>{
        val oldValue = this.value
        backingValue = value
        registry.trigger(value)
        registry.clear()
        changedHook.trigger(ValueUpdate(oldValue, value))
        return this
    }

    fun reset() {
        backingValue = null
        registry.clear()
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
    typeData: Typed<T>,
): BackingContainerBase<T>(typeData) {

    fun onValueSet(callback:(Change<T?, T>)-> Unit){
        changedHook.subscribe(callback)
    }


    override fun toString(): String = "BackingContainer<${typeData.typeName}>"

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


class LazyContainer<T: Any>(
    typeData: Typed<T>
):BackingContainerBase<T>(typeData) {

    var lockedForEdit: Boolean = false
        private set

    override var backingValue: T? = null
    val registry: NotifierRegistry<T> = NotifierRegistry()

    internal var notifierConfig: NotificationConfig = NotificationConfig()

    fun onValueSet(callback:(Change<T?, T>)-> Unit){
        changedHook.subscribe(callback)
    }

    fun requestValue(subscriber: CTX, callback:(T)-> Unit){
        backingValue?.let {
            callback.invoke(it)
        }?:run {
            registry.subscribe(subscriber, callback)
        }
    }

    fun <T2 : Any> requestValueCasting(
        subscriber: CTX,
        kClass: KClass<T2>,
        callback: (T2) -> Unit
    ) {
        subscriber.notify("requestValueCasting")
        val value = backingValue
        if (value != null) {
            notify("value != null invoking")
            val casted = value.safeCast(kClass)
            if (casted != null) {
                callback.invoke(casted)
            } else {
                notify("Cast to ${kClass.simpleName} failed wount invoke", SeverityLevel.WARNING)
            }
        }else{
            registry.subscribe(subscriber, subscriber.identity.numericId, callback as (T) -> Unit)
        }
    }

    override fun provideValue(value: T):LazyContainer<T> {
        if(!lockedForEdit){
            super.provideValue(value)
            registry.trigger(value)
            lockedForEdit = true
            registry.clear()
        }else{

        }
        return this
    }
}

inline fun <reified T: Any> lazyContainerOf(
    configBuilder:NotificationConfig.()-> Unit
):LazyContainer<T>{

   val config = NotificationConfig()
    config.configBuilder()
    val container = LazyContainer(TypeData.create<T>())



    return container
}


inline fun <reified T: Any> lazyContainerOf(
    initialValue:T? = null
):LazyContainer<T>{

    val container = LazyContainer(TypeData.create<T>())
    initialValue?.let {
        container.provideValue(it)
    }
    return container
}

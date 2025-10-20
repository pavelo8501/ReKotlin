package po.misc.containers

import po.misc.context.CTX
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.ManagedException
import po.misc.exceptions.managedException
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.common.Fallback
import po.misc.functions.hooks.Change
import po.misc.functions.hooks.ChangeHook
import po.misc.functions.hooks.models.ValueUpdate
import po.misc.functions.registries.NotifierRegistry
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken


abstract class BackingContainerBase<T: Any>(
    private val host: Any,
    val typeData: TypeToken<T>
): LogEmitter{

    enum class EmissionType{
        EmmitOnce,
        EmmitAlways
    }

    abstract val emissionType: EmissionType

    private var backingValue: T? = null
    protected var provider:(() ->T)? = null

    /**
     * Returns the current source value.
     * @throws ManagedException if the backing value is not available.
     */
    var value:T?
        get() = backingValue
        protected set(value) {
            backingValue = value
        }

    /**
     * Indicates whether the source backing value has been provided.
     */
    val isAvailable: Boolean get() = backingValue != null

    private var exceptionFallback: ExceptionFallback? = null

    private val registry = NotifierRegistry<T>(host, "BackingContainer")

    protected open val  valueAccessErrMsg = "Value<${typeData.typeName}> accessed before initialization"

    protected var changedHook: ChangeHook<T> = ChangeHook()

    private fun getValueWithFallback(callingContext: Any): T {
        val currentValue = value
        if (currentValue != null) {
            return currentValue
        } else {
            exceptionFallback?.exceptionProvider?.invoke()
        }
        return value.getOrManaged(callingContext, typeData.kClass)
    }

    private val valueNullMsg: (String)-> String = {name->
        "Context $name is trying to access uninitialized value in $this"
    }

    protected abstract fun valueAvailable(value:T)

    private fun notifyValueNull(callingContext: Any){
       val msg = when(callingContext){
            is CTX-> valueNullMsg(callingContext.identifiedByName)
            else ->  valueNullMsg(callingContext::class.simpleName.toString())
        }
        callingContext.notify(msg, SeverityLevel.WARNING)
    }

    private fun applyValue(newValue:T): Int{
        value = newValue
        val listenersCount = registry.subscriptionsCount
        registry.trigger(newValue)
        if(emissionType == EmissionType.EmmitOnce){
            registry.clear()
        }
        valueAvailable(newValue)
        return listenersCount
    }

    fun returnValue():T?{
        return value ?: run {
            val valueProvider = provider
            if (valueProvider != null) {
                val valueFromProvider = valueProvider()
                value = valueFromProvider
                valueFromProvider
            } else {
                null
            }
        }
    }

    fun provideFallback(fallback: ExceptionFallback) {
        exceptionFallback = fallback
    }

    fun getValue(callingContext: Any): T {
        return returnValue().getOrThrow {
            notifyValueNull(callingContext)
            managedException(valueAccessErrMsg)
        }
    }

    fun getValue(context: TraceableContext):T = getValue(callingContext =  context)

    fun getValue(callingContext: Any, callback: (T)-> Unit){
        returnValue()?.let {
            callback.invoke(it)
        }?:run {
            val index = registry.subscriptionsCount + 1L
            registry.subscribe(callingContext, index, callback)
        }
    }

    @Deprecated("Change to getValue()")
    fun requestValue(subscriber: Any, callback: (T)-> Unit) = getValue(subscriber, callback)

    fun <T2: Any> getWithFallback(fallback: Fallback<T2>):T{
        val value = backingValue
        if(value != null){
            return value
        }
        val fallbackValue = fallback.initiateFallback()

       val castedFallbackValue = when(this){
            is BackingContainer->{
                fallbackValue.castOrManaged(this, typeData.kClass)
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
    open fun provideValue(value:T):BackingContainerBase<T>{
        val oldValue = this.value
        applyValue(value)
        changedHook.trigger(ValueUpdate(oldValue, value))
        return this
    }


    open fun  provide(valueProvider:() ->T):BackingContainerBase<T>{
        provider = valueProvider
        if(registry.subscriptionsCount > 0){
           val newValue = valueProvider()
           applyValue(newValue)
        }
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
 * is set will trigger a failure via the [ThrowableCallSitePayload].
 *
 * @param T the type of the backing value.
 * @property exPayload a payload used to report failures if the source is accessed prematurely.
 * @property typeData type metadata used for diagnostics or error reporting.
 * @constructor Creates a [BackingContainer] with optional initial [sourceBacking].
 */
open class BackingContainer<T: Any>(
    owner: Any,
    typeData: TypeToken<T>,
): BackingContainerBase<T>(owner, typeData) {

    override val emissionType: EmissionType = EmissionType.EmmitAlways

    override fun valueAvailable(value:T){

    }

    fun onValueSet(callback: (Change<T?, T>) -> Unit) {
        changedHook.subscribe(callback)
    }

    override fun toString(): String = "BackingContainer<${typeData.typeName}>"

    companion object {
        inline fun <reified T : Any> create(
            owner: Any, initialValue: T? = null
        ): BackingContainer<T> {
            val container = BackingContainer(owner, TypeToken.create<T>())
            if (initialValue != null) {
                container.provideValue(initialValue)
            }
            return container
        }
    }
}

inline fun <reified T: Any> Any.backingContainerOf():BackingContainer<T>{
   return BackingContainer(this, TypeToken.create<T>())
}

fun <T: Any> Any.backingContainerOf(
    typeData: TypeToken<T>
):BackingContainer<T>{
    return BackingContainer(this, typeData)
}

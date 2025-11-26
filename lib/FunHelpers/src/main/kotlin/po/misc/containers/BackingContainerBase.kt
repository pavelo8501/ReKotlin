package po.misc.containers

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.containers.backing.BackingContainer
import po.misc.containers.lazy.LazyContainer
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.ClassResolver
import po.misc.exceptions.managedException
import po.misc.functions.LambdaOptions
import po.misc.functions.NoResult
import po.misc.functions.hooks.ChangeHook
import po.misc.functions.hooks.models.ValueUpdate
import po.misc.functions.models.NotificationConfig
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken

/**
 * The current resolved value of the container, or `null` if the
 * container has not yet been initialized.
 *
 * This property never triggers lambda evaluation; use [getValue]
 * to safely obtain the value with full lazy semantics.
 */
abstract class BackingContainerBase<T : Any>(
    override val typeToken: TypeToken<T>
): Component,  BackingBuilder<T>, Tokenized<T> {

    enum class EmissionType{
        EmmitOnce,
        EmmitAlways
    }

    private val readingValueSubject: String = "Reading Value"

    abstract var emissionType: EmissionType
        protected set

    override var config: NotificationConfig = NotificationConfig()

    private var valueBacking: T? = null

    val value: T?
        get() = valueBacking?:run {
            fallbackValueProvider?.let {
                warn(readingValueSubject, "Using fallback to provide result")
                it.invoke()
            }
        }




    protected val valueProvided: Signal<T, Unit> = signalOf(NoResult)


    protected var provider: (() ->T)? = null
    protected var fallbackValueProvider :  (() ->T)? = null
    val fallbackAvailable: Boolean get() =  fallbackValueProvider != null

    /**
     * Indicates whether the source backing value has been provided.
     */
    val valueAvailable: Boolean get() = valueBacking != null

    protected open val  valueAccessErrMsg: String = "${this::class.simpleName}'s value<${typeToken.typeName}> accessed before initialization"

    protected var changedHook: ChangeHook<T> = ChangeHook()

    private val valueNullMsg: (String) -> String = { name->
        "Context $name is trying to access uninitialized value<${typeToken.typeName}> in ${this::class.simpleName}"
    }

    protected abstract fun valueAvailable(value:T)

    private fun applyValue(newValue:T): T{
        val oldValue = valueBacking
        valueBacking = newValue
        changedHook.trigger(ValueUpdate(oldValue, newValue))
        valueProvided.trigger(newValue)
        valueAvailable(newValue)
        return newValue
    }

    private fun prepareValue():T?{
        val valueByBacking = valueBacking
        if(valueByBacking != null){
            return valueByBacking
        }
        val valueByProvider = provider?.invoke()
        if(valueByProvider != null){
            valueBacking = valueByProvider
            return valueByProvider
        }
        return value
    }

    override fun valueProvided(listener: TraceableContext, callback: (T)-> Unit){
        valueProvided.onSignal(listener, callback)
    }

    /**
     * Resolves the container’s value.
     *
     * - If a value is already stored, it is returned immediately.
     * - If a lazy initializer is present, it is evaluated and the result
     *   becomes the stored value.
     * - If no value or initializer exists, a structured diagnostic trace
     *   is emitted and an [IllegalStateException] is thrown.
     *
     * @throws IllegalStateException if the value has not been provided
     */
    @Throws(IllegalStateException::class)
    fun getValue(callingContext: TraceableContext):T{
        val returnValue = prepareValue()
        if(returnValue != null){
            return returnValue
        }else{
            warn(readingValueSubject, valueNullMsg(ClassResolver.classInfo(callingContext).formattedString))
            val exception = IllegalStateException(valueAccessErrMsg)
            callingContext.managedException(exception, immediateOutput = true)
            throw exception
        }
    }


    fun getValue(listener: TraceableContext, callback: (T)-> Unit){
        val returnValue = prepareValue()
        if(returnValue != null){
            callback.invoke(returnValue)
        }else{
            when(this){
                is BackingContainer-> valueProvided.onSignal(listener, LambdaOptions.Listen, callback)
                is LazyContainer -> valueProvided.onSignal(listener, LambdaOptions.Promise, callback)
            }
        }
    }

    fun forget(listener: TraceableContext){
        valueProvided.listeners.remove(listener)
    }

    fun getWithFallback(fallback: ()-> T):T{
        val returnValue = valueBacking
        if(returnValue != null){
            return returnValue
        }
        return fallback()
    }

    override fun provideValue(newValue:T, allowOverwrite: Boolean):BackingContainerBase<T>{

        if(allowOverwrite){
            applyValue(newValue)
        }else{
            if(valueBacking == null){
                applyValue(newValue)
            }
        }
        return this
    }

    override fun provideValue(type : EmissionType, valueProvider:() ->T):BackingContainerBase<T>{
        emissionType = type
        provider = valueProvider
        if(valueProvided.listeners.isNotEmpty()){
           val value =  prepareValue()
           if(value != null){
               valueProvided.trigger(value)
               applyValue(value)
           }
        }
        return this
    }

    override fun setFallback(provider: ()-> T){
        fallbackValueProvider = provider
    }

    /**
     * Resets the container to an uninitialized state.
     *
     * This operation:
     * - clears the stored value,
     * - removes any queued `valueProvided` listeners,
     * - clears the lazy provider lambda.
     *
     * After reset, the container behaves exactly as if newly created.
     */
    fun reset() {
        valueBacking = null
        valueProvided.listeners.clear()
    }
}

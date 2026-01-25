package po.misc.callbacks.callable

import po.misc.collections.asList
import po.misc.collections.lambda_map.CallableParameter
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.debugging.stack_tracer.extractTrace
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import po.misc.functions.CallableKey
import po.misc.functions.CallableType
import po.misc.functions.LambdaOptions
import po.misc.functions.PropertyKind
import po.misc.functions.PropertyKind.Mutable
import po.misc.functions.PropertyKind.Readonly
import po.misc.functions.Sync
import po.misc.interfaces.named.NamedComponent
import po.misc.reflection.displayName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

sealed interface CallableDescriptor<in T, out R>: TokenizedResolver<@UnsafeVariance T, @UnsafeVariance R>, NamedComponent{

    val isSuspended: Boolean

    val parameterType: TypeToken<@UnsafeVariance T>
    val resultType: TypeToken<@UnsafeVariance R>

    override val sourceType: TypeToken<@UnsafeVariance T>
        get() = parameterType

    override val receiverType: TypeToken<@UnsafeVariance R>
        get() = resultType

    val parameters: CallableParameter
    val callableType: CallableType
    val callableKey: CallableKey get() = callableType.callableKey
    val isCollection: Boolean get() = resultType.isCollection
}

interface DataResolver<in T, out R>: CallableDescriptor<T, R> {
    fun call(parameter: T): R
}

interface DataProvider<in T, out R>: CallableDescriptor<T, R>{
    fun call(): R
}

sealed interface ReceiverCallable<in T, out R>: DataResolver<T, R>{
    override val isSuspended: Boolean get() = false


    override fun call(parameter: T): R
    fun call(parameter: T, param: Unit): R {
        return call(parameter)
    }
}

sealed class FunctionCallableBase<T, R>(
    override val parameterType: TypeToken<T>,
    override val resultType: TypeToken<R>,
    val callableMeta: CallableMeta
):ReceiverCallable<T, R>{

    override val parameters: CallableParameter = CallableParameter()
    val options: LambdaOptions = LambdaOptions.Listen
    override val callableType: CallableType = Sync
    override val name: String = callableMeta.functionName
    override val displayName: TextSpan = StyledPair(name)

    protected fun makeCall(onError: ((CallSiteReport) -> Unit)? = null,  block: ()-> R):R{
        return try {
            block.invoke()
        }catch(ex: Throwable){
            val trace =  ex.extractTrace(TraceOptions.Method(callableMeta , parameters.outputOnException))
            onError?.invoke(trace.callSite())
            throw ex
        }
    }
}

class FunctionCallable<T, R>(
    override val parameterType: TypeToken<T>,
    override val resultType: TypeToken<R>,
    val function : Function1<T,  R>,
):FunctionCallableBase<T, R>(parameterType, resultType, CallableMeta(function::class, resultType.kClass)), CallableCollection<T, R> {

    override val callableKey: CallableKey = CallableKey.Resolver
    override val callableType: CallableType = Sync

    override val callableList: List<ReceiverCallable<T, R>> = asList()

    fun call(parameter: T, onError: (CallSiteReport) -> Unit): R = makeCall(onError) { function.invoke(parameter) }
    override fun call(parameter: T): R = makeCall { function.invoke(parameter) }

    companion object: TokenFactory{
        inline operator fun <reified T, reified R> invoke(
            noinline function : Function1<T, R>
        ):FunctionCallable<T, R>{
            return FunctionCallable(tokenOf<T>(), tokenOf<R>(), function)
        }
        inline operator fun <T, reified R> invoke(
            receiverType: TypeToken<T>,
            noinline function : Function1<T, R>
        ):FunctionCallable<T, R> = FunctionCallable(receiverType, tokenOf<R>(), function)
    }
}


class ProviderCallable<T, R>(
    override val parameterType: TypeToken<T>,
    override val resultType: TypeToken<R>,
    private  val function : Function0<R>
):FunctionCallableBase<T, R>(parameterType, resultType, CallableMeta(function::class, null)), DataProvider<T, R> {

    override val callableKey: CallableKey = CallableKey.Provider
    override val callableType: CallableType = Sync

    override fun call(parameter: T): R = call()
    override fun call(): R = makeCall { function.invoke() }
    fun call(onError: (CallSiteReport) ->  Unit): R = makeCall(onError) { function.invoke() }

    companion object: TokenFactory{
        inline operator fun <reified T, reified R> invoke(
            noinline function : Function0<R>
        ):ProviderCallable<T, R> = ProviderCallable(tokenOf<T>(), tokenOf<R>(), function)

        inline operator fun <T, reified R> invoke(
            receiverType: TypeToken<T>,
            noinline function : Function0<R>
        ):ProviderCallable<T, R> = ProviderCallable(receiverType, tokenOf<R>(), function)
    }
}

class PropertyCallable<T, R>(
    override val parameterType: TypeToken<T>,
    override val resultType: TypeToken<R>,
    private val property : KProperty1<T, R>
):ReceiverCallable<T, R>, CallableCollection<T, R>{

    override val parameters: CallableParameter = CallableParameter()
    override var callableType: PropertyKind = Readonly

    override val name: String = property.name
    override val displayName: StyledPair =  StyledPair(name,  property.displayName)
    override val callableList: List<PropertyCallable<T, R>> = this.asList()

    init {
        callableType =  if(property is KMutableProperty1<*, *>){ Mutable
        }else{ Readonly }
    }
    override fun call(parameter: T): R = property.get(parameter)
    override fun equals(other: Any?): Boolean {
        if(other !is PropertyCallable<*, *>) return false
        return property == other.property
    }
    override fun hashCode(): Int {
        val result = property.hashCode()
        return result
    }
    companion object: TokenFactory{
        inline operator fun <reified T, reified R> invoke(
            property : KProperty1<T, R>,
            callableCompanion: Companion = PropertyCallable
        ):PropertyCallable<T, R> = PropertyCallable(tokenOf<T>(), tokenOf<R>(), property)

        inline operator fun <T, reified R> invoke(
            receiverType: TypeToken<T>,
            property : KProperty1<T, R>,

        ):PropertyCallable<T, R> = PropertyCallable(receiverType, tokenOf<R>(), property)

    }
}

class ProviderProperty<T, R>(
    override val parameterType: TypeToken<T>,
    override val resultType: TypeToken<R>,
    private val property : KProperty0<R>
):ReceiverCallable<T, R>, CallableCollection<T, R>,  DataProvider<T, R>{

    override val parameters: CallableParameter = CallableParameter()
    override var callableType: PropertyKind = Readonly

    override val name: String = property.name
    override val displayName: StyledPair = StyledPair(name, property.displayName)
    override val callableList: List<ProviderProperty<T, R>> = this.asList()

    init {
        callableType =  if(property is KMutableProperty1<*, *>){ Mutable
        }else{ Readonly }
    }

    override fun call(): R = property.get()
    override fun call(parameter: T): R = call()

    override fun equals(other: Any?): Boolean {
        if(other !is ProviderProperty<*, *>) return false
        return property == other.property
    }
    override fun hashCode(): Int {
        val result = property.hashCode()
        return result
    }
    companion object: TokenFactory{
        inline operator fun <reified T, reified R> invoke(
            property : KProperty0<R>,
            callableCompanion: Companion = ProviderProperty
        ):ProviderProperty<T, R> = ProviderProperty(tokenOf<T>(), tokenOf<R>(), property)

        inline operator fun <T, reified R> invoke(
            receiverType: TypeToken<T>,
            property : KProperty0<R>
        ):ProviderProperty<T, R> = ProviderProperty(receiverType, tokenOf<R>(), property)
    }
}







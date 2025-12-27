package po.misc.collections.lambda_map

import po.misc.functions.CallableOptions
import po.misc.functions.LambdaOptions
import po.misc.functions.PropertyKind
import po.misc.functions.Readonly
import po.misc.reflection.displayName
import po.misc.types.k_function.lambdaName
import po.misc.types.token.TokenFactory
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


sealed interface CallableDescriptor<in T, out R>: TokenizedResolver<@UnsafeVariance T, @UnsafeVariance R>{
    enum class CallableKey{ReadOnlyProperty, MutableProperty, Resolver }
    val name: String
    val displayName:String
    val options: CallableOptions
    val isSuspended: Boolean
    override val receiverType: TypeToken<@UnsafeVariance T>
    override val valueType: TypeToken<@UnsafeVariance R>
    val callableKey: CallableKey

    fun keyEquals(key: CallableKey): Boolean {
       return when(key){
            CallableKey.MutableProperty -> {
                callableKey == CallableKey.MutableProperty
            }
            CallableKey.ReadOnlyProperty -> {
                callableKey == CallableKey.ReadOnlyProperty || callableKey == CallableKey.MutableProperty
            }
            else -> key == callableKey
        }
    }
}

sealed interface ReceiverCallable<in T, out R>: CallableDescriptor<T, R>{
    override val isSuspended: Boolean get() = false
    fun call(value: T): R
    fun call(value: T, param: Unit): R {
        return call(value)
    }
}

class FunctionCallable<T, R>(
    override val receiverType: TypeToken<T>,
    override val valueType: TypeToken<R>,
    private val function : Function1<T, R>,
):ReceiverCallable<T, R>{
    override val callableKey: CallableDescriptor.CallableKey = CallableDescriptor.CallableKey.Resolver

    override val options: LambdaOptions = LambdaOptions.Listen
    override val name: String = function::class.lambdaName
    override val displayName:String = name
    override fun call(value: T): R =  function.invoke(value)

    companion object: TokenFactory{
        inline operator fun <reified T, reified R> invoke(
            noinline function : Function1<T, R>
        ):FunctionCallable<T, R>{
           return FunctionCallable(tokenOf<T>(), tokenOf<R>(), function)
        }
    }
}

class PropertyCallable<T, R>(
    override val receiverType: TypeToken<T>,
    override val valueType: TypeToken<R>,
    private val property : KProperty1<T, R>
):ReceiverCallable<T, R>{

    override var callableKey: CallableDescriptor.CallableKey = CallableDescriptor.CallableKey.ReadOnlyProperty
        private set
    override val options: PropertyKind = Readonly
    override val name: String = property.name
    override val displayName: String = property.displayName
    init {
        if(property is KMutableProperty1<*, *>){
            callableKey = CallableDescriptor.CallableKey.MutableProperty
        }
    }
    override fun call(value: T): R = property.get(value)
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
            property : KProperty1<T, R>
        ):PropertyCallable<T, R>{
            return PropertyCallable(tokenOf<T>(), tokenOf<R>(), property)
        }
    }
}
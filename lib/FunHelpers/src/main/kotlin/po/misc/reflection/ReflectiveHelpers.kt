package po.misc.reflection

import po.misc.data.output.output
import po.misc.types.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

inline fun <reified T: Any, reified A: Annotation> getAnnotated(): List<KProperty<*>> {
    val kClass = T::class
    val annotated = kClass.memberProperties.filter { it.hasAnnotation<A>() }
    return annotated
}

data class PropertyLookup(
    val receiverClass: KClass<out Any>?
){

    constructor(javaClass: Class<*>):this(javaClass.kotlin)

    internal val propertiesBacking: MutableList<KProperty<*>> = mutableListOf()
    val properties: List<KProperty<*>> get() = propertiesBacking

    internal val errorsBacking: MutableList<Throwable> = mutableListOf()
    val errors: MutableList<Throwable> get() = errorsBacking

    internal val propertyReadouts: MutableList<Pair<String, String>> = mutableListOf()
    val hasFailures: Boolean get() = errors.isNotEmpty()

    fun addProperty(property : KProperty<*>): PropertyLookup{
        propertiesBacking.add(property)
        return this
    }

    fun registerData(data: Any?, property : KProperty<*>): PropertyLookup{
        propertyReadouts.add(Pair(data.toString(), property.name))
        return this
    }

    fun registerData(data: Any?, name: String): PropertyLookup{
        propertyReadouts.add(Pair(data.toString(), name))
        return this
    }

    fun registerThrowable(th : Throwable): PropertyLookup{
        errorsBacking.add(th)
        return this
    }
}

@PublishedApi
internal inline fun <reified V: Any>  getPropertyImpl(
    receiver: Any,
    outputErrors: Boolean = false,
    noinline onFailureAction : ((PropertyLookup)-> Unit)? = null
): KProperty1<Any, V>? {
    val returnValueClass = V::class
    val receiverClass = receiver::class
    val propertyLookupFailResult = PropertyLookup(receiverClass)
    val propertyList = receiverClass.memberProperties.toList()

    @Suppress("UNCHECKED_CAST")
    for (i in  0 ..< propertyList.size){
        try {
            val property  = propertyList[i] as KProperty1<Any, Any>
            val result = property.get(receiver)
            propertyLookupFailResult.addProperty(property)
            if(result::class.isSubclassOf(returnValueClass)){
                return property.safeCast<KProperty1<Any, V>>()
            }
        }catch (th: Throwable){
            if(outputErrors){
                th.output()
            }
            propertyLookupFailResult.registerThrowable(th)
        }
    }
    onFailureAction?.invoke(propertyLookupFailResult)
    return null
}

inline fun <reified V: Any> getProperty(
    receiver: Any,
    outputErrors: Boolean = false
): KProperty1<Any, V>? = getPropertyImpl(receiver, outputErrors)

inline fun <reified V: Any> getProperty(
    receiver: Any,
    noinline onFailureAction : (PropertyLookup)-> Unit
): KProperty1<Any, V>? = getPropertyImpl(receiver, onFailureAction = onFailureAction)



package po.misc.reflection

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaGetter


interface KProperty0Container{
    val property:  KProperty0<Any>
}

data class NameValuePair(
    val name: String,
    val value: String
)



data class KProperty0Data(
    override val property:  KProperty0<Any>,
): KProperty0Container{
    val name: String get() = property.name
    var value: String = ""

    fun updateValue(newValue: String):String{
        value = newValue
        return newValue
    }
}

fun updateData(receiver: Any,  propertyList: List<KProperty0Data>){
    propertyList.forEach {data->
        val value =   data.property.getValue(receiver, data.property).toString()
        data.updateValue(value)
    }
}


internal fun playDirty(
    receiver: Any,
    prop: KProperty<Any>,
    lookupResult : PropertyLookup
): Any? {

    val receiverClass = receiver::class
    val field = runCatching {
        receiverClass.java.getDeclaredField(prop.name).apply { isAccessible = true }
    }.getOrNull()

    val value = try {
        when {
            field != null -> field.get(receiver)
            prop.javaGetter != null -> {
                prop.javaGetter!!.apply { isAccessible = true }.invoke(receiver)
            }
            else -> error("No field or getter for property ${prop.name}")
        }
    }catch (ex: Throwable){
        lookupResult.registerThrowable(ex)
        null
    }
    lookupResult.registerData(value, prop)
    return value
}

fun <T: Any> T.updateK0Data(
    propertyList: List<KProperty0Data>,
    onFailureAction : ((PropertyLookup)-> Unit)? = null
){
    val receiverClass = this::class
    val receiver: Any = this
    val lookupResult = PropertyLookup(receiverClass)
    propertyList.forEach {data->
        val prop = data.property
        playDirty(receiver,  prop, lookupResult)?.let {
            data.updateValue(it.toString())
        }
    }
    if (lookupResult.hasFailures) {
        onFailureAction?.invoke(lookupResult)
    }
}



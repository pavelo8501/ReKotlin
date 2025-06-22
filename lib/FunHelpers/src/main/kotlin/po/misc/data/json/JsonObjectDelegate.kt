package po.misc.data.json

import po.misc.data.interfaces.Printable
import po.misc.exceptions.ManagedException
import po.misc.types.castOrThrow
import po.misc.types.getOrManaged
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


inline fun <reified P, T: Any, reified S: Printable>  P.jsonDelegatePart(
    sourceProperty : KProperty1<S, Any>,
    vararg templateProperties: KProperty1<T, Any>
):JsonObjectDelegate<T, P, S> where P: JsonDescriptor<*> {

    return JsonObjectDelegate(this,sourceProperty,  templateProperties.toList())
}

class JsonObjectDelegate<T: Any, P, S: Printable>(
    val parent: P,
    val  sourceProperty: KProperty1<S, Any>,
    val properties: List<KProperty1<T, Any>>
): ReadOnlyProperty<Any?, String> where P: JsonDescriptor<*>  {

    var  propertyParam: KProperty<String>? = null
    val  property: KProperty<String> get() = propertyParam.getOrManaged("Value is null")

    val propertyName: String get() = property.name
    var receiver: T? = null

    val jsonObject: JObject by lazy { JObject(propertyName) }
    val jsonString: String get() = jsonObject.toString()

    fun resolveProperty(property: KProperty<*>){
        if(propertyParam == null) {
            propertyParam =
                property.castOrThrow<KProperty<String>, ManagedException>("Unable to cast KProperty<*> to KProperty<F_DTO>")
            parent.subscribeForReceiver {receiver->
                val subObject = sourceProperty.get(receiver as S)
                val resultList : MutableList<String> = mutableListOf()
                properties.forEach {
                 buildString {
                        val castedReceiver = subObject as T
                        val value =  it.invoke(castedReceiver)
                        jsonObject.addRecord(JRecord(it.name, "${value}ms"))
                    }
                }
                this
            }
        }
    }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): String {
        return jsonString
    }
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>):JsonObjectDelegate<T, P, S> {
        resolveProperty(property)
        return this
    }

}

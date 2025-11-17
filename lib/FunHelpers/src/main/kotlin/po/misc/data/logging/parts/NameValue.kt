package po.misc.data.logging.parts

import po.misc.data.HasKeyValuePair
import po.misc.reflection.getAnnotated
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


class KeyValue(
    override val name: String,
    override val value: String
): HasKeyValuePair {
    override fun toString(): String  = pairStr
}

class NameValueTable(vararg val pairs: HasKeyValuePair){
    val pairsList = mutableListOf<HasKeyValuePair>()

    init {
        val list = pairs.toList()
        pairsList.addAll(list)
    }
}


class PropertyValue(
    val property: KProperty<*>,
    val typeToken: TypeToken<*>,
) {
    val name: String  get() = property.name
    var receiverBacking: Any? = null

    val receiver: Any get() = receiverBacking.getOrManaged(this)

    fun <T: Any> applyKeysValues(receiver:T): KeyValue {
        val result = property.safeCast<KProperty1<Any, *>>()?.let { casted ->
            casted.get(receiver).toString()
        } ?: "N/A"
        return KeyValue(property.name, result.toString())

    }
}

class ReflectiveTable< T: Any >(
    val typeToken: TypeToken<T>,
): TokenFactory{

    val pairsList = mutableListOf<PropertyValue>()

    fun addKey(
        property: KProperty<*>
    ):ReflectiveTable<T>{
        val propertyValuePair = PropertyValue(property,  typeToken)
        pairsList.add(propertyValuePair)
        return this
    }

    fun <T: Any> applyKeysValues(receiver:T): List<KeyValue> {
        return   pairsList.map {  it.applyKeysValues(receiver) }
    }

    companion object{
        inline operator fun <reified T: Any> invoke(receiver:T):ReflectiveTable<T>{
            val table =  ReflectiveTable<T>(TypeToken.create())
            val result = getAnnotated<T, ValueSnapshot>()
            result.forEach {
                table.addKey(it)
            }
           return table
        }
    }
}

inline fun <reified T: Any> T.reflectiveTable(
    builder: ReflectiveTable<T>.(T)-> Unit
): ReflectiveTable<T>{
   val table =  ReflectiveTable<T>(TypeToken.create())
   builder.invoke(table, this)
   return table
}







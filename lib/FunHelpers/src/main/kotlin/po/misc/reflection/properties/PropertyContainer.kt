package po.misc.reflection.properties

import po.misc.types.safeCast
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

interface PropertyContainer<T: Any> {

    val containerName: String
    val receiverSimpleName : String
    val receiverQualifiedName : String

    val hasReceiver: Boolean
    fun addProperty(propertyInfo: PropertyInfo<T>)

    fun updateData(data:T)
    fun readData():T
}

inline fun <reified T: Any>  T.createContainer(name: String): PropertyAccess<T> {
    val container = PropertyAccess<T>(name, this)
    T::class.memberProperties.forEach { kProperty ->
        kProperty.safeCast<KProperty1<T, Any>>()?.toPropertyInfo(this, container) ?: println("Skipped${kProperty}")
    }
    return container
}





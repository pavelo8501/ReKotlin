package po.misc.reflection.properties.mappers.models

import po.misc.exceptions.ManagedException
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface PropertyContainer<T: Any> {
    val propertyName: String
    val property: KProperty<T>
}

data class PropertyRecord<T: Any>(
    override val propertyName: String,
    override val property: KProperty<T>
):PropertyContainer<T>{

    fun asKProperty1():KProperty1<T, *>{
        val message = "Unable to cast KProperty<T> to <KProperty1<T, *> in PropertyHelpers.kt"
        return property.castOrThrow<KProperty1<T, *>, ManagedException>()
    }


    companion object{

        fun <T: Any> create(property: KProperty1<T, *>):PropertyRecord<T>{
            val message = "Unable to cast KProperty1<T, *> to <KProperty<T> in PropertyHelpers.kt"
           return PropertyRecord(property.name, property.castOrThrow<KProperty<T>, ManagedException>(message))
        }

        fun <T: Any> create(property: KProperty<T>):PropertyRecord<T>{
            return PropertyRecord(property.name, property)
        }
    }

}
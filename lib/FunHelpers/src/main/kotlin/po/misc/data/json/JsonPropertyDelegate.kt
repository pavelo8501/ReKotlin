package po.misc.data.json

import po.misc.data.PrintableBase
import po.misc.exceptions.ManagedException
import po.misc.time.ExecutionTimeStamp
import po.misc.types.castOrThrow
import po.misc.types.getOrManaged
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


interface SerializationProvider<R: Any?>{
    fun serialize(name: String,  value: R): String
}


abstract class JsonDelegateBase<T: Any, R: Any>(
    val kProperty: KProperty1<T, R>,
    val provider: SerializationProvider<R> = object : SerializationProvider<R>{
        override fun serialize(name: String,  value: R): String = value.toString()
    }
) {

    var  propertyParam: KProperty<R>? = null
    val  property: KProperty<R> get() = propertyParam.getOrManaged<KProperty<R>, ManagedException>("Value is null")


    val propertyName: String get() = property.name
    var receiver: T? = null
    var value: R? = null

    fun getJasonString():R{
       return value.getOrManaged<R, ManagedException>("Value is null")
    }

    fun resolveProperty(property: KProperty<*>){
        if(propertyParam == null) {
            propertyParam =
                property.castOrThrow<KProperty<R>, ManagedException>("Unable to cast KProperty<*> to KProperty<F_DTO>")

        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
        resolveProperty(property)
        (thisRef as JsonDescriptor<T>).registerDelegate(this as JsonDelegateBase<T, *>)
        return getJasonString()
    }


    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>):JsonDelegateBase<T, R> {
        resolveProperty(property)
        (thisRef as JsonDescriptor<T>).registerDelegate(this as JsonDelegateBase<T, *>)
        return this
    }

    open fun toJson(receiver: T): JRecord {
        val rawValue = kProperty.get(receiver)
        val result = provider.serialize(propertyName, rawValue)
        return JRecord(propertyName, rawValue)
    }
}

class JsonDelegate<T: Any, V: Any>(
    kProperty: KProperty1<T, V>,
    provider:  SerializationProvider<V>
): JsonDelegateBase<T, V>(kProperty, provider){

}




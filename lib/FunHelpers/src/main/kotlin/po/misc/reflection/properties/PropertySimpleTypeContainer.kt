package po.misc.reflection.properties

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


sealed interface PropertySimpleTypeContainer<T: Any>

sealed interface MutableProperty<T: Any, V: Any>{
    fun updateValue(receiver:T, value: V)
}

class StringPropertyContainer<T: Any>(
    val property: KMutableProperty1<T, String>
):PropertySimpleTypeContainer<T>, MutableProperty<T, String> {
    override fun updateValue(receiver:T, value: String){
        property.set(receiver, value)
    }
}




class ReadOnlyStringProperty<T: Any>(
    val property: KProperty1<T, String>
):PropertySimpleTypeContainer<T>{

    fun readValue(receiver:T): String{
       return property.get(receiver)
    }
}


class LongPropertyContainer<T: Any>(val property: KMutableProperty1<T, Long>):PropertySimpleTypeContainer<T>{
    fun updateValue(receiver:T, value: Long){
        property.set(receiver, value)
    }
}

class IntPropertyContainer<T: Any>(
    val property: KMutableProperty1<T, Int>
):PropertySimpleTypeContainer<T>,  MutableProperty<T, Int>{

    override fun updateValue(receiver:T, value: Int){
        property.set(receiver, value)
    }
}

class ReadOnlyIntProperty<T: Any>(val property: KProperty1<T, Int>):PropertySimpleTypeContainer<T>{
    fun readValue(receiver:T): Int{
        return property.get(receiver)
    }
}

class BoolPropertyContainer<T: Any>(val property: KMutableProperty1<T, Boolean>):PropertySimpleTypeContainer<T>{
    fun updateValue(receiver:T, value: Boolean){
        property.set(receiver, value)
    }
}

package po.misc.registries.type

import po.misc.interfaces.ValueBased
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class TypeRecord<T: Any>(
    val element : ValueBased,
    val clazz: KClass<T>,
    val kType: KType,
){
    val simpleName get() = clazz.simpleName.toString()
    val qualifiedName get() = clazz.qualifiedName.toString()

    companion object{
        inline fun <reified T: Any> createRecord(element: ValueBased):TypeRecord<T>{
           return TypeRecord(element, T::class, typeOf<T>())
        }
    }

}


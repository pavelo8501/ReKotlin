package po.misc.types

import po.misc.interfaces.ValueBased
import po.misc.interfaces.ValueBasedClass
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class TypeRecord<T: Any>(
    val type : ValueBased,
    val clazz: KClass<T>,
    val kType: KType,
){

    val typeKey : String = kType.toSimpleNormalizedKey()
    val simpleName : String get() = clazz.simpleName.toString()
    val qualifiedName: String get() = clazz.qualifiedName.toString()

    companion object{
        inline fun <reified T: Any> createRecord(element: ValueBased):TypeRecord<T>{
           return TypeRecord(element, T::class, typeOf<T>())
        }


    }

}
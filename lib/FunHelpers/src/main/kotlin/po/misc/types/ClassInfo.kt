package po.misc.types

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ClassInfo<T: Any>(
val clazz: KClass<T>,
val kType: KType,
){
    val typeKey : String = kType.toSimpleNormalizedKey()
    val simpleName : String get() = clazz.simpleName.toString()
    val qualifiedName: String get() = clazz.qualifiedName.toString()

    companion object{
        inline fun <reified T: Any> createInfo():ClassInfo<T>{
            return ClassInfo(T::class, typeOf<T>())
        }
    }

}
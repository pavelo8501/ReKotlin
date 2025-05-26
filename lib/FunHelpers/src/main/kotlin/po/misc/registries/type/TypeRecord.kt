package po.misc.registries.type

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class TypeRecord<T: Any>(
    val name : String,
    val clazz: KClass<T>,
    val kType: KType,
){
    val simpleName get() = clazz.simpleName.toString()
    val qualifyedName = "$name[$simpleName]"
}
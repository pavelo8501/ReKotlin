package po.misc.types

import po.misc.collections.ComparableType
import po.misc.collections.StaticTypeKey
import po.misc.interfaces.ValueBased
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

interface Typed<T: Any>: ComparableType<T>{
    override val kClass: KClass<T>
    val kType: KType
}


@Deprecated("Replace by TypeData", level= DeprecationLevel.WARNING)
data class TypeRecord<T: Any>(
    val type : ValueBased,
    override val kClass: KClass<T>,
    override val kType: KType,
):Typed<T>{

    val typeKey : String = kType.toSimpleNormalizedKey()
    val simpleName : String get() = kClass.simpleName.toString()
    val qualifiedName: String get() = kClass.qualifiedName.toString()

    override val typeName: String get() = typeKey

    fun  toTypeData(): TypeData<T>{
        return TypeData(kClass, kType)
    }
    fun toStaticTypeKey(): StaticTypeKey<T>{
        return  StaticTypeKey<T>(kClass)
    }
    companion object{
        inline fun <reified T: Any> createRecord(element: ValueBased):TypeRecord<T>{
           return TypeRecord(element, T::class, typeOf<T>())
        }
        fun <T: Any> createRecord(element: ValueBased, clazz: KClass<T>):TypeRecord<T>{
            val type =  clazz.createType()
           return TypeRecord(element, clazz, type)
        }
    }
}
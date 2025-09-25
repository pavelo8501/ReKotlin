package po.misc.types

import po.misc.collections.ComparableType
import po.misc.collections.StaticTypeKey
import po.misc.interfaces.ValueBased
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

//interface Typed<T: Any>: ComparableType<T>{
//    override val kClass: KClass<T>
//    val kType: KType
//}



interface Typed<T: Any>{
    val parameter1: TypeData<T>
}



interface DoubleTyped<T1: Any, T2: Any>{
   val parameter1: TypeData<T1>
   val parameter2: TypeData<T2>
}










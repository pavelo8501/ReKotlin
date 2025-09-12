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

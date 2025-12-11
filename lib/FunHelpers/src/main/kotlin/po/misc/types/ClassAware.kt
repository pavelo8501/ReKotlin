package po.misc.types

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties




interface ClassAware<T> {
    val kClass: KClass<T & Any>
}

val ClassAware<*>.memberProperties: Collection<KProperty1<*, *>>get() = kClass.memberProperties


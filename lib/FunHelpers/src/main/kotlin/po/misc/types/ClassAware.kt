package po.misc.types

import kotlin.reflect.KClass

interface ClassAware<T> {
    val kClass: KClass<T & Any>
}
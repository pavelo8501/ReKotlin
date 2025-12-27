package po.misc.types

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties


interface ClassAware<T> {
    val kClass: KClass<T & Any>
}

interface TypeHolder<T> : ClassAware<T> {
    val kType: KType
    val isCollection: Boolean get() = kType.classifier == List::class
}

fun <T> Any.safeCast(
    classAware: ClassAware<T>
):T? {
    return safeCast<T>(classAware.kClass)
}


val ClassAware<*>.memberProperties: Collection<KProperty1<*, *>>get() = kClass.memberProperties


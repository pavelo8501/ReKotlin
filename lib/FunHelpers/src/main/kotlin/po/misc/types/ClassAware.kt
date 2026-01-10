package po.misc.types

import po.misc.types.k_class.simpleOrAnon
import po.misc.types.token.TypeProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties


interface ClassAware<T> {
    val kClass: KClass<T & Any>
}

interface TypeHolder<T> : ClassAware<T>, TypeProvider {
    override val kClass: KClass<T & Any>
    val kType: KType
    val isCollection: Boolean get() = kType.classifier == List::class

    override val typeName: String get() = kClass.simpleOrAnon
}

fun <T> Any.safeCast(
    classAware: ClassAware<T>
):T? {
    return safeCast<T>(classAware.kClass)
}


val ClassAware<*>.memberProperties: Collection<KProperty1<*, *>>get() = kClass.memberProperties


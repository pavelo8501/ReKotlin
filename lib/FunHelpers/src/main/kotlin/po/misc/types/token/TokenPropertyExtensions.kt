package po.misc.types.token

import po.misc.reflection.PropertyLookup
import po.misc.reflection.Readonly
import po.misc.reflection.ReflectiveAssassin
import po.misc.types.ClassAware
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.memberProperties


fun <T: Any> TypeToken<T>.resolveProperty(
    kind: Readonly,
    propertyName: String,
): KProperty1<T, *>? {
    val kClass = this.kClass
    return kClass.memberProperties.firstOrNull { it.name == propertyName }?.safeCast<KProperty1<T, *>>()
}

fun <T: Any, V: Any> TypeToken<T>.resolveProperty(
    kind: Readonly,
    propertyName: String,
    valueClass: ClassAware<V>
): KProperty1<T, V>? {
    val kClass = this.kClass
    return kClass.memberProperties.firstOrNull { it.name == propertyName }?.safeCast<KProperty1<T, V>>()
}

fun <T: Any> TypeToken<T>.resolveProperty(
    kind: Readonly,
    property: KProperty<*>,
): KProperty1<T, *>? = resolveProperty(kind, property.name)


fun <T: Any, V: Any> TypeToken<T>.resolveProperty(
    kind: Readonly,
    property: KProperty<*>,
    valueClass: ClassAware<V>
): KProperty1<T, V>? = resolveProperty(kind, property.name, valueClass)
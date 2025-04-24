package po.exposify.extensions

import po.exposify.classes.interfaces.DataModel
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties

class ReflectionHelpers {
}

inline fun <reified DATA: DataModel, T: KType,  V> DATA.getPropertyByValue(
    type: T,
    value:V): KProperty1<DATA, V>? {
    return this::class.memberProperties.asSequence()
        .filter { it.returnType.isSubtypeOf(type)}
        .filterIsInstance<KProperty1<DATA, V>>()
        .firstOrNull{it.get(this) == value}
}
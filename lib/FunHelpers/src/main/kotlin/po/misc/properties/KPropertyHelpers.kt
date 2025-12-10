package po.misc.properties

import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf


@PublishedApi
internal fun <T: Any, V> checkPropertyType(
    property: KProperty1<*, *>,
    expectedType: KType,
):KProperty1<T, V>?{
    val actualType = property.returnType
    if (actualType.isSubtypeOf(expectedType)) {
        return property.safeCast<KProperty1<T, V>>()
    }
    return null
}

inline fun <reified T: Any, reified V> KProperty1<*, *>.checkType(): KProperty1<T, V>? {
    return checkPropertyType<T, V>(this,  typeOf<V>())
}

inline fun <reified T: Any, V> KProperty1<*, *>.checkType(
    returnType: TypeToken<V>,
): KProperty1<T, V>? {
    return checkPropertyType<T, V>(this,  returnType.kType)
}

fun <T: Any, V> KProperty1<*, *>.checkType(
    receiverType: TypeToken<T>,
    returnType: TypeToken<V>,
): KProperty1<T, V>? {
    return checkPropertyType<T, V>(this,  returnType.kType)
}

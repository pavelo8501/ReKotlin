package po.misc.serialization

import kotlinx.serialization.KSerializer
import po.misc.types.getKType
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@JvmName("toSerializerInfoAttached")
inline fun <reified T> KSerializer<T>.toSerializerInfo(
    isListSerializer: Boolean = true
): SerializerInfo<T> {
    val type =  typeOf<T>()
    val key = type.toSimpleNormalizedKey()
    return SerializerInfo(key, this, type, isListSerializer)
}


//inline fun <reified T> toSerializerInfo(
//    serializer: KSerializer<T>,
//    isListSerializer: Boolean = true
//): SerializerInfo<T> {
//    val type =  typeOf<T>()
//    val key = type.toSimpleNormalizedKey()
//    return SerializerInfo(key, serializer, type, isListSerializer)
//}
//

fun <T: Any> toSerializerInfo(
    type: KType,
    serializer: KSerializer<T>,
    isListSerializer: Boolean
): SerializerInfo<T> {
    val key = type.toSimpleNormalizedKey()
    return SerializerInfo(key, serializer, type, isListSerializer)
}

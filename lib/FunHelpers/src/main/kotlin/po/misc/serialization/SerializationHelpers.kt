package po.misc.serialization

import kotlinx.serialization.KSerializer
import po.misc.types.toSimpleNormalizedKey
import po.misc.types.token.TypeToken
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@JvmName("toSerializerInfoAttached")
inline fun <reified T> KSerializer<T>.toSerializerInfo(
    isListSerializer: Boolean = true
): SerializerInfo<T> {
    val type =  typeOf<T>()
    val key = type.toSimpleNormalizedKey()
    @Suppress("UNCHECKED_CAST")
    val typeData: TypeToken<KSerializer<T>> = TypeToken.create()
    return SerializerInfo(typeData, key, this, type, isListSerializer)
}

fun <T: Any> toSerializerInfo(
    type: KType,
    serializer: KSerializer<T>,
    isListSerializer: Boolean
): SerializerInfo<T> {
    @Suppress("UNCHECKED_CAST")
    val typeData: TypeToken<KSerializer<T>> = TypeToken.create()
    return SerializerInfo(typeData, type.toSimpleNormalizedKey(), serializer, type, isListSerializer)
}

package po.misc.serialization

import kotlinx.serialization.KSerializer
import po.misc.types.token.TypeToken
import kotlin.reflect.KType

data class SerializerInfo<T>(
    val serializerTypeData: TypeToken<KSerializer<T>>,
    val normalizedKey: String,
    val serializer : KSerializer<T>,
    val serializableClass : KType,
    val isListSerializer: Boolean = true
){
    init {
        val a = normalizedKey
    }
}
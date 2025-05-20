package po.misc.serialization

import kotlinx.serialization.KSerializer
import kotlin.reflect.KType

data class SerializerInfo<T>(
    val normalizedKey: String,
    val serializer : KSerializer<T>,
    val serializableClass : KType,
    val isListSerializer: Boolean = true
){
    init {
        val a = normalizedKey
    }
}
package po.misc.serialization

import kotlinx.serialization.KSerializer
import po.misc.types.type_data.TypeData
import kotlin.reflect.KType

data class SerializerInfo<T>(
    val serializerTypeData: TypeData<KSerializer<T>>,
    val normalizedKey: String,
    val serializer : KSerializer<T>,
    val serializableClass : KType,
    val isListSerializer: Boolean = true
){
    init {
        val a = normalizedKey
    }
}
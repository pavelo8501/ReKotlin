package po.wswraptor.components.serializationfactory.models

import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.KSerializer

data class SerializerContainer<P: Any>(
    val routName: String,
    val typeInfo: TypeInfo,
    val kSerializer: KSerializer<P>,
)

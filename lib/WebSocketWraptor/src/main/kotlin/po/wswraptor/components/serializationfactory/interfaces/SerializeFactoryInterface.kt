package po.wswraptor.components.serializationfactory.interfaces

import io.ktor.util.reflect.TypeInfo
import po.wswraptor.models.request.WSMessage

interface SerializeFactoryInterface {

    fun <C: Any>register( resourceName: String)

    fun <T:WSMessage<*>>serialize(typeInfo: TypeInfo, value : Any): String?
    fun <T: Any>deserialize(data: String, typeInfo: TypeInfo? = null): WSMessage<T>?
}
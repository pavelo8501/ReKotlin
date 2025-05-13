package po.exposify.dao.classes

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer


@Serializable
abstract class JSONBType<T: Any>(
    private var serializerProvider:()-> KSerializer<T>
){
    val serializer: KSerializer<T> get() = serializerProvider()
    val listSerializer: KSerializer<List<T>> get() = ListSerializer(serializerProvider())
}
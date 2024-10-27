package po.api.ws_service.service.models


import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.w3c.dom.TypeInfo
import po.api.ws_service.service.models.WSApiRequestDataInterface
import kotlin.reflect.KClass


enum class ApiRequestAction(val value: Int){
    UNKNOWN(0),
    CREATE(1),
    UPDATE(2),
    SELECT(3),
    DELETE(4)
}

class RequestDataSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<T> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: T) = dataSerializer.serialize(encoder, value)
    override fun deserialize(decoder: Decoder) = (dataSerializer.deserialize(decoder))
}


//class RequestDataObjectSerializer<T>(private val dataSerializer: KSerializer<T>) : JsonContentPolymorphicSerializer<ApiRequestDataType<T>> {
//    override val descriptor: SerialDescriptor = dataSerializer.descriptor
//    override fun serialize(encoder: Encoder, value: WSApiRequestDataInterface<T>) = dataSerializer.serialize(encoder,
//        value as T
//    )
//    override fun deserialize(decoder: Decoder) = (dataSerializer.deserialize(decoder))
//}


interface WSApiRequestDataInterface{


}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ApiRequestDataType(){

}

@Serializable
@SerialName("create")
data class CreateRequest<out D : WSApiRequestDataInterface>(
    @Contextual
    val resource: D
):ApiRequestDataType(){
}

@Serializable
@SerialName("update")
data class UpdateRequest<D>(
   @Contextual
   val resource: D
):ApiRequestDataType()

@Serializable
@SerialName("select")
data class SelectRequest(
   val ids: List<Long>
):ApiRequestDataType()

@Serializable
@SerialName("delete")
data class DeleteRequest(
    val id: Long
) : ApiRequestDataType()

interface WSApiRequestBaseContext{
    val module : String
    val action : ApiRequestAction
}

@Serializable
data class WSApiRequestBase(
    override val module : String,
    override val action : ApiRequestAction,
):WSApiRequestBaseContext

@Serializable
data class WSApiRequest<T :ApiRequestDataType>(

    override val module : String,
    override val action : ApiRequestAction,
    var data : T?,

):WSApiRequestBaseContext{
    var dataAsJson : JsonElement? = null

}
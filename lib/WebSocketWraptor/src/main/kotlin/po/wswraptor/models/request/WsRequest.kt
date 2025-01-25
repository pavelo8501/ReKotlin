package po.wswraptor.models.request

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import po.wswraptor.models.response.WsResponse

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

interface WSApiRequestDataInterface{
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ApiRequestDataType()

@Serializable
@SerialName("entity")
data class EntityBasedRequest<out D : WSApiRequestDataInterface>(
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
data class WsRequest<T :ApiRequestDataType>(
    override val module : String,
    override val action : ApiRequestAction,
    var data : T?
):WSApiRequestBaseContext{

    var requestJson : String = ""

    var dataAsJson : JsonElement? = null

    fun setSourceJson(json: String){
        requestJson = json
    }

    fun <R>toResponse(result:R): WsResponse<R>{
        val response = WsResponse<R>(this as WsRequest<ApiRequestDataType>, result)
        return response
    }

}
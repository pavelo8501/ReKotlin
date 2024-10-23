package po.api.ws_service.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import po.api.rest_service.common.ApiEntity
import po.api.rest_service.common.ApiUpdateEntity
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.CreateRequestData


enum class ApiRequestAction(val value: Int){
    UNKNOWN(0),
    CREATE(1),
    UPDATE(2),
    SELECT(3),
    DELETE(4)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RequestData()

@Serializable
@SerialName("create")
data class CreateRequestData(
   val resource: JsonElement
):RequestData()

@Serializable
@SerialName("update")
data class UpdateRequest(
   @Contextual
   val resource: JsonElement
):RequestData()


data class SelectRequestData(
   val ids: List<Long>
):RequestData()

@Serializable
@SerialName("delete")
data class DeleteRequestData(val id: Long ) : RequestData()

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
data class WSApiRequest(

    override val module : String,
    override val action : ApiRequestAction,
    var data : JsonElement,

):WSApiRequestBaseContext{

    fun <R>extractData(requestType : KSerializer<R>){

    }

}
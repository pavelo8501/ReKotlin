package po.api.ws_service.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator
import po.api.rest_service.common.ApiEntity
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.CreateRequestData
import po.api.rest_service.models.RequestData


@Serializable
data class WSApiRequest<R : RequestData>(
   val module : String,
   val action : String,
   var data : R
)
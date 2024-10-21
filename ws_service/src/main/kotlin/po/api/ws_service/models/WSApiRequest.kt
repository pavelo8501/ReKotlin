package po.api.ws_service.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import po.api.rest_service.common.ApiEntity
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.CreateRequestData
import po.api.rest_service.models.RequestData


//@OptIn(ExperimentalSerializationApi::class)
//@Serializable
//@JsonClassDiscriminator("type")
//sealed class RequestData
//
//@Serializable
//@SerialName("create")
//data class CreateRequestData(val value: ApiEntity) : RequestData()
//
//@Serializable
//@SerialName("select")
//data class SelectRequestData(val value: Long) : RequestData()

data class WSApiRequest<R : RequestData>(override var data : R) : ApiRequest<R>(data)
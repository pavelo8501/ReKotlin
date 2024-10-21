package po.api.rest_service.models

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator
import po.api.rest_service.common.ApiDeleteEntity

import po.api.rest_service.common.ApiEntity
import po.api.rest_service.common.ApiLoginRequestDataContext
import po.api.rest_service.common.ApiUpdateEntity


@Serializable
data class DefaultLoginRequest(
    override val username: String,
    override val password: String
) : ApiLoginRequestDataContext

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RequestData

@Serializable
@SerialName("create")
data class CreateRequestData(val value: ApiEntity) : RequestData()

@Serializable
@SerialName("update")
data class UpdateRequestData(val value: ApiUpdateEntity) : RequestData()


@Serializable
@SerialName("select")
data class SelectRequestData(val value: Long) : RequestData()

@Serializable
@SerialName("delete")
data class DeleteRequestData(val value: ApiDeleteEntity) : RequestData()

@Serializable
@SerialName("credentials")
data class LoginRequestData(val value: DefaultLoginRequest) : RequestData()

@Serializable
open data class ApiRequest<R : RequestData>(
    val data : R
)
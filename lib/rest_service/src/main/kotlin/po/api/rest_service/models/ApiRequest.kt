package po.api.rest_service.models

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

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
data class UpdateRequestData(val value: ApiUpdateEntity) : RequestData()


@Serializable
data class SelectRequestData(
    val value: Long
)


@Serializable
data class DeleteRequestData(
    val id: Long,
    val includingChild: Boolean
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class ApiRequest<R>(
    val data : R
)
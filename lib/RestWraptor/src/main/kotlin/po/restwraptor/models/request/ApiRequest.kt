package po.restwraptor.models.request

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import po.restwraptor.interfaces.EntityInterface
import po.restwraptor.interfaces.LoginRequestInterface
import po.restwraptor.interfaces.UpdateEntityInterface


@Serializable
data class DefaultLoginRequest(
    override val username: String,
    override val password: String
) : LoginRequestInterface

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RequestData

@Serializable
@SerialName("create")
data class CreateRequestData(val value: EntityInterface) : RequestData()

@Serializable
data class UpdateRequestData(val value: UpdateEntityInterface) : RequestData()

@Serializable
data class SelectRequestData(val value: Long)

@Serializable
data class DeleteRequestData(val id: Long, val includingChild: Boolean)

@Serializable
data class LoginRequest(val username: String, val password: String
)

@Serializable
data class ApiRequest<R>(val data : R)


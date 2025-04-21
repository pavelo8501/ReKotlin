package po.restwraptor.models.request


import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val login: String, val password: String)

@Serializable
data class LogoutRequest(val login: String)

@Serializable
data class ApiRequest<R>(val data : R)


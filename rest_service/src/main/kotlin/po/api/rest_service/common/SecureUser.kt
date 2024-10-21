package po.api.rest_service.common

interface SecureUserContext {

    val username: String
    val roles: List<String>

    fun toPayload(): String

}
package po.api.rest_service.common

interface SecureUser {

    val username: String
    val password: String
    val roles: List<String>

}
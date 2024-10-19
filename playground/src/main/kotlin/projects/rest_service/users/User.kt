package po.playground.projects.rest_service.users

import kotlinx.serialization.Serializable
import po.api.rest_service.common.SecureUser

data class User(

    override val username: String,
    override val password: String

) : SecureUser {
    override val roles: List<String> = listOf("user")
    var id: Int = 0
    var email: String = ""

    override fun toPayload(): String {
        return "{ \"id\": $id, \"email\": \"$email\" }"
    }
}
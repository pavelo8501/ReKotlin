package po.playground.projects.rest_service.users

import kotlinx.serialization.Serializable


//data class User(
//
//    override val username: String,
//    val password: String
//
//) : SecureUserContext {
//    override val roles: List<String> = listOf("user")
//    var id: Int = 0
//    var email: String = ""
//
//    override fun toPayload(): String {
//        return "{ \"id\": $id, \"email\": \"$email\" }"
//    }
//}
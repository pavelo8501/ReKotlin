package po.auth.authentication.authenticator.models


/**
 * Represents a minimal authenticated user.
 */
interface AuthenticationPrincipal : SerializablePrincipal {
    val id: Long
    val login: String
    val hashedPassword: String
    val email: String
    val userGroupId: Long
}

interface SerializablePrincipal {
    fun asJson(): String
}

//@Serializable
//open class SessionPrincipal(
//    override val id: Long = 0L,
//    override val login: String = "no_name",
//    override val hashedPassword: String,
//    override val email: String = "nomail@undeliverable.void",
//    override val userGroupId: Long = 0L,
//) : AuthenticationPrincipal {
//
//    override fun asJson(): String {
//        return Json.encodeToString(this)
//    }
//}

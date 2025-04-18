package po.auth.authentication.interfaces

/**
 * Represents a minimal authenticated user.
 */
interface AuthenticationPrincipal {
    val userId: Long
    val login: String
    val email: String
    val userGroupId: Long
    val roles: Set<String>
}




interface SerializablePrincipal : AuthenticationPrincipal {

     fun asJson(): String
}
package po.auth.authentication.interfaces

/**
 * Represents a minimal authenticated user.
 */
interface AuthenticationPrincipal : SerializablePrincipal {
    val id: Long
    val login: String
    val email: String
    val userGroupId: Long
}

interface SerializablePrincipal {
     fun asJson(): String
}
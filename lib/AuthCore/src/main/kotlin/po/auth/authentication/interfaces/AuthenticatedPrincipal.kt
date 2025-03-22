package po.auth.authentication.interfaces

/**
 * Represents a minimal authenticated user.
 */
interface AuthenticatedPrincipal {
    val userId: Long
    val username: String
    val email: String
    val userGroupId: Long
    val roles: Set<String>

    fun asJson(): String
}
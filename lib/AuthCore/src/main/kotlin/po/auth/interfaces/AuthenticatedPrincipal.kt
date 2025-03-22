package po.auth.interfaces


/**
 * Represents a minimal authenticated user.
 */
interface AuthenticatedPrincipal {
    val userId: Long
    val username: String
    val userGroupId: Long
    val roles: Set<String>
}
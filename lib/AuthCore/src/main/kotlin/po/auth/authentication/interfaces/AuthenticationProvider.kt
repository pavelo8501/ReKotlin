package po.auth.authentication.interfaces

import po.auth.models.AuthenticationContext

/**
 * Interface for authenticating a call and generating a user session.
 */
interface AuthenticationProvider {
    suspend fun authenticate(context: AuthenticationContext): AuthenticatedPrincipal?
}
package po.auth.authentication.authenticator.interfaces

import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.models.AuthorizedSession

/**
 * Interface for authenticating a call and generating a user session.
 */
interface AuthenticationProvider {
    suspend fun authenticate(authData: AuthenticationData):  AuthorizedSession
}
package po.auth.authentication.authenticator.interfaces

import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession

/**
 * Interface for authenticating a call and generating a user session.
 */
interface AuthenticationProvider {
    fun authorize(authData: SessionIdentified):  AuthorizedSession
}
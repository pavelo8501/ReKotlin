package po.auth.extensions

import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.models.SessionDefaultIdentity
import po.auth.sessions.models.AuthorizedSession

suspend fun AuthorizedSession.authenticate(login: String, password: String): AuthenticationPrincipal
    = AuthSessionManager.authenticator.authenticate(login, password, this)

fun generatePassword(password: String) = AuthSessionManager.authenticator.generatePasswordHash(password)


fun registerAuthenticator(userLookupFn:  (login: String)-> AuthenticationPrincipal?) =
    AuthSessionManager.registerAuthenticator(userLookupFn)


fun createDefaultIdentifier():SessionDefaultIdentity{
   return SessionDefaultIdentity()
}
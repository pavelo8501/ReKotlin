package po.auth.sessions.classes

import po.auth.*
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.lognotify.extensions.newTask
import po.lognotify.extensions.startTask
import po.lognotify.extensions.subTask
import java.util.concurrent.ConcurrentHashMap

class SessionFactory(
    private val manager: AuthSessionManager,
    private  val internalStorage : ConcurrentHashMap<String, String>
) : TasksManaged
{

    internal val activeSessions : ConcurrentHashMap<String, AuthorizedSession> = ConcurrentHashMap<String, AuthorizedSession>()

    suspend fun createAuthorizedSession(sessionId: String,  principal: AuthorizedPrincipal, authenticator : UserAuthenticator) : AuthorizedSession
        = subTask("createAuthorizedSession") {

       val anonSession = activeSessions[sessionId].getOrThrow("session with id $sessionId not found", ErrorCodes.SESSION_NOT_FOUND)
       anonSession.providePrincipal(principal)

    }.resultOrException()

    suspend fun createAnonymousSession(authData : AuthenticationData,  authenticator : UserAuthenticator): AuthorizedSession
        = newTask("createAnonymousSession") {
            val anonSession = AuthorizedSession(authData.remoteAddress, authenticator)
            activeSessions[anonSession.sessionId] = anonSession
            anonSession
    }.resultOrException()

}
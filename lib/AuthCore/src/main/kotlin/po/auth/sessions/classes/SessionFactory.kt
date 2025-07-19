package po.auth.sessions.classes

import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.context.Identifiable
import java.util.concurrent.ConcurrentHashMap

class SessionFactory(
    private val manager: AuthSessionManager,
    private  val internalStorage : ConcurrentHashMap<String, String>
) : TasksManaged, Identifiable {
    override val contextName: String = "SessionFactory"

    private val activeSessions : ConcurrentHashMap<String, AuthorizedSession> = ConcurrentHashMap<String, AuthorizedSession>()

    fun sessionLookUp(sessionId: String):AuthorizedSession?{
        return activeSessions[sessionId]
    }

    fun listAnonymous(): List<AuthorizedSession>{
        return activeSessions.values.filter { it.sessionType == SessionType.ANONYMOUS}
    }

    fun createAnonymousSession(authData : SessionIdentified,  authenticator : UserAuthenticator): AuthorizedSession{
       val anonSession = AuthorizedSession(authData.remoteAddress, authenticator)
       activeSessions[anonSession.sessionID] = anonSession
       return anonSession
    }

}
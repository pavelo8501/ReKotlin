package po.auth.sessions.classes

import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.models.RoundTripData
import po.auth.models.SessionOrigin
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.context.Identifiable
import java.util.concurrent.ConcurrentHashMap

class SessionFactory(
    private val manager: AuthSessionManager,
    private  val internalStorage : ConcurrentHashMap<String, String>
){

    private val activeSessions : ConcurrentHashMap<SessionIdentified, AuthorizedSession> = ConcurrentHashMap<SessionIdentified, AuthorizedSession>()
    fun sessionLookUp(authData: SessionIdentified):AuthorizedSession?{
        return activeSessions[authData]
    }
    fun listAnonymous(): List<AuthorizedSession>{
        return activeSessions.values.filter { it.sessionType == SessionType.ANONYMOUS}
    }
    fun createAnonymousSession(authData : SessionIdentified): AuthorizedSession{
        val session = AuthorizedSession(authData)
        session.roundTripInfo.add(RoundTripData(0, SessionOrigin.ReCreated))
       return  AuthorizedSession(authData)
    }

}
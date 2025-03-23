package po.auth.sessions.classes

import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticatedPrincipal
import po.auth.sessions.interfaces.AuthenticatedSession
import po.auth.sessions.interfaces.AuthorizedPrincipal
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.interfaces.asAuthorized
import po.auth.sessions.models.AnonymousPrincipal
import po.auth.sessions.models.BaseSessionAbstraction
import po.auth.sessions.models.DefaultSession
import java.util.concurrent.ConcurrentHashMap

class SessionFactory(
    private val manager: AuthSessionManager,
    private  val internalStorage : ConcurrentHashMap<String, String>) {

    private val activeSessions : ConcurrentHashMap<String, DefaultSession> = ConcurrentHashMap<String, DefaultSession>()
    private var activeAnonSession  : AnonymousSession? = null

    fun createSession(principal: AuthenticatedPrincipal) : DefaultSession {
       val newSession = DefaultSession(internalStorage ).apply {
            setPrincipal(principal.asAuthorized(this))
        }
        activeSessions[newSession.sessionId] = newSession
        return newSession
    }

    fun createAnonymousSession(anonymous : AuthenticatedPrincipal? = null) :  AnonymousSession? {
      val anonymousPrincipal =  if(anonymous != null){
            AnonymousPrincipal(anonymous) } else { AnonymousPrincipal(null) }
        activeAnonSession  = AnonymousSession(
            anonymousPrincipal,
            internalStorage,
            BaseSessionAbstraction.generateSessionId(),
            AuthorizedPrincipal.Key
        )
        return activeAnonSession
    }
    fun getAnonymousSession(): AnonymousSession?{
        return  activeAnonSession
    }

    fun activeSessions(): List<DefaultSession>{
        return  activeSessions.values.toList()
    }

}
package po.auth.sessions.classes

import po.auth.*
import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import java.util.concurrent.ConcurrentHashMap

class SessionFactory(
    private val manager: AuthSessionManager,
    private  val internalStorage : ConcurrentHashMap<String, String>) {

    private val activeSessions : ConcurrentHashMap<String, AuthorizedSession> = ConcurrentHashMap<String, AuthorizedSession>()
    private var activeAnonSession  : AuthorizedSession? = null

    fun createSession(principal: AuthorizedPrincipal) : AuthorizedSession {
        try {
            val newSession = AuthorizedSession(principal, SessionType.USER_AUTHENTICATED, internalStorage)
            activeSessions[newSession.sessionId] = newSession
            return newSession
        } catch (ex: Exception) {
            echo(ex)
            throw (ex)
        }


        fun createAnonymousSession(anonymous: AuthenticationPrincipal? = null): AuthorizedSession {
            try {
                if (activeAnonSession != null) {
                    return activeAnonSession!!
                }
                val anonymousPrincipal = if (anonymous != null) {
                    AuthorizedPrincipal().copyReinit(anonymous)
                } else {
                    AuthorizedPrincipal()
                }
                val anonSession = AuthorizedSession(anonymousPrincipal, SessionType.ANONYMOUS, internalStorage)
                activeAnonSession = anonSession
                return anonSession
            } catch (ex: Exception) {
                echo(ex)
                throw (ex)
            }
        }

        fun getAnonymousSession(): AuthorizedSession? {
            return activeAnonSession
        }

        fun activeSessions(): List<AuthorizedSession> {
            return activeSessions.values.toList()
        }
    }

    fun createAnonymousSession(anonymousUser: AuthenticationPrincipal?): AuthorizedSession{
        if(anonymousUser != null){
            val principal = AuthorizedPrincipal().copyReinit(anonymousUser)
            activeAnonSession = AuthorizedSession(principal, SessionType.ANONYMOUS, ConcurrentHashMap<String, String>())
        }else{
            activeAnonSession = AuthorizedSession(AuthorizedPrincipal(), SessionType.ANONYMOUS, ConcurrentHashMap<String, String>())
        }
        return activeAnonSession!!
    }

    fun activeSessions(): List<AuthorizedSession>{
        return  activeSessions.values.toList()
    }

    fun getAnonymousSession(): AuthorizedSession?{
        val anonSession = activeAnonSession
        if(anonSession?.sessionType == SessionType.ANONYMOUS){
            return  anonSession
        }
        return null
    }



}
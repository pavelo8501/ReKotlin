package po.auth.sessions.models

import po.auth.AuthSessionManager
import po.auth.sessions.interfaces.EmmitableSession
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement


class AuthorizedSession(
    val internalStore : ConcurrentHashMap<String, String>,
    override val principal : AuthorizedPrincipal
):  AbstractCoroutineContextElement(AuthSessionManager.PrincipalKey),  EmmitableSession{


    //override val principal : AuthorizedPrincipal  by lazy { principal }
    override val sessionId: String = io.ktor.server.sessions.generateSessionId()


    fun setPrincipal(principal : AuthorizedPrincipal){
       // newPrincipal = principal
    }


    override fun onProcessStart(session: EmmitableSession) {
        TODO("Not yet implemented")
    }

    override fun onProcessEnd(session: EmmitableSession) {
        TODO("Not yet implemented")
    }

}




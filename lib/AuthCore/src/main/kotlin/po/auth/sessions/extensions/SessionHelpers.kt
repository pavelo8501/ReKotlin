package po.auth.sessions.extensions

import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.sessions.enumerators.IdentifiedBy
import po.auth.sessions.models.AuthorizedSession


//suspend inline fun <reified T : CoroutineScope> T.withSession(session: AuthorizedSession){
//
//    val clazzName = this::class.simpleName
//    println(clazzName)
//
//    withContext(session) {
//        val sessionBefore = coroutineContext[AuthorizedSession]
//        println(sessionBefore?.sessionId?:"Firs Nope")
//        val clazzName = this::class.simpleName
//        println(clazzName)
//        val sessionAfter= coroutineContext[AuthorizedSession]
//        println(sessionAfter?.sessionId?:"Second Nope")
//    }
//}




//suspend fun withSession(identity: String, identifiedBy: IdentifiedBy,  block: suspend AuthorizedSession.() -> Unit){
//    val session = AuthSessionManager.getActiveSessions().firstOrNull{ it.sessionId == identity }.getOrThrow("Session for id: $identity not found",
//        ErrorCodes.SESSION_NOT_FOUND)
//    withContext(session) { session.block() }
//}


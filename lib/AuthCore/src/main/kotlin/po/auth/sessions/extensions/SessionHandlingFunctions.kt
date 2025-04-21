package po.auth.sessions.extensions

import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession
import kotlin.coroutines.CoroutineContext





fun ApplicationCall.withCoroutineContext(session: AuthorizedSession): CoroutineContext {
    return coroutineContext + session
}


fun ApplicationCall.withCoroutineContext2(session: AuthorizedSession): ApplicationCall {
    return object : ApplicationCall by this {
        override val coroutineContext: CoroutineContext
            get() = this@withCoroutineContext2.coroutineContext + session
    }
}



//suspend fun AuthorizedSession.snapshot(label: String = "SESSION SNAPSHOT") {
//    val YELLOW = "\u001B[33m"
//    val RESET = "\u001B[0m"
//
//    println("""
//        $YELLOW
//        --- [$label] ---
//        🔑 sessionId     = $sessionId
//        🧑 principal      = ${principal.login} (${principal.userId})
//        🧠 sessionType    = $sessionType
//        🗂️  internalStore = ${internalStore.keys.joinToString(", ")}
//        🎯 registeredHandlers = ${
//          getAttributeKeys().joinToString(", ")
//    }
//        🧵 coroutineScope = ${coroutineScope() scope.coroutineContext[Job]?.let { if (it.isActive) "ACTIVE" else "INACTIVE" }}
//        ---------------------
//        $RESET
//    """.trimIndent())
//}


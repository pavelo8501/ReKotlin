package po.restwraptor.extensions

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.path
import io.ktor.util.AttributeKey
import io.ktor.util.toMap
import kotlinx.coroutines.withContext
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.extensions.session
import po.auth.models.SessionDefaultIdentity
import po.auth.sessions.models.AuthorizedSession
import po.misc.context.CTX
import po.misc.exceptions.TraceableContext
import po.misc.types.getOrThrow
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.ExceptionCodes


fun ApplicationCall.authData(): AuthenticationData{
    val headersMap: Map<String, String> = request.headers.toMap().mapValues { (_, value) ->
        value.joinToString(",")
    }

    return AuthenticationData(
        request.headers[WraptorHeaders.XAuthToken.value] ?:"",
        request.origin.remoteHost,
        headersMap,
        request.path(),
        request.headers[WraptorHeaders.XAuthToken.value]?:"",
        )
}

//suspend fun resolveSessionFromHeader(call: ApplicationCall): AuthorizedSession{
//    val authData = call.authData()
//    val session = AuthSessionManager.getOrCreateSession(authData)
//    call.sessionToAttributes(session)
//    return session
//}
//


fun <T :ApplicationCall>  T.authSessionOrNull():AuthorizedSession?{
    val authorizedSessionKey = AttributeKey<AuthorizedSession>("AuthSession")
    val session =  attributes.takeOrNull(authorizedSessionKey)
    return session
}

private fun createDefaultSession(): AuthorizedSession{
    val identity: SessionDefaultIdentity = SessionDefaultIdentity()
   return session(identity)

}

fun <T :ApplicationCall> T.currentSessionOrNew():AuthorizedSession {
    val authorizedSessionKey = AttributeKey<AuthorizedSession>("AuthSession")
    return attributes.takeOrNull(authorizedSessionKey) ?: run {
        createDefaultSession()
    }
}

suspend fun <T :ApplicationCall, R>  T.withSession(
    context: TraceableContext,
    block : suspend AuthorizedSession.()-> R
):R{
   val authorizedSessionKey = AttributeKey<AuthorizedSession>("AuthSession")
   val session =  attributes.takeOrNull(authorizedSessionKey)
   val checked = session.getOrThrow(context){message->
       ConfigurationException(context, "$message Session missing", ExceptionCodes.GENERAL_AUTH_CONFIG_FAILURE)
   }
   return withContext(this.coroutineContext){
        block(checked)
    }
}






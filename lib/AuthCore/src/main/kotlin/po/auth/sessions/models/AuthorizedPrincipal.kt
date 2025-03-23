package po.auth.sessions.models


import po.auth.AuthSessionManager
import po.auth.authentication.interfaces.AuthenticationPrincipal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement


data class AuthorizedPrincipal(
    override val userId: Long,
    override val username: String,
    override val email: String,
    override val userGroupId: Long,
    override val roles: Set<String>,
    val session: AuthorizedSession,
    val internalStore: ConcurrentHashMap<String, String>,
    val sessionId: String,
) : AuthenticationPrincipal{


    companion object {
        fun generateSessionId(): String = UUID.randomUUID().toString()
    }
}


//
//
//data class AuthorizedPrincipal(
//    override val userId: Long,
//    override val username: String,
//    override val email: String,
//    override val userGroupId: Long,
//    override val roles: Set<String>,
//    val session: EmmitableSession
//) : AuthorizedPrincipal, CoroutineContext.Element {
//    override val hostSession get() = session
//    override val key = AuthorizedPrincipalKey
//}
//
//data class DefaultSession(
//    override val sessionId: String = UUID.randomUUID().toString()
//) : EmmitableSession, CoroutineContext.Element {
//    override val key = SessionKey
//}
//
//suspend fun withAuthenticatedSession(
//    principal: AuthorizedPrincipal,
//    session: EmmitableSession,
//    block: suspend CoroutineScope.() -> Unit
//) {
//    withContext(session + principal, block)
//}
//
//suspend fun resolvePrincipal(call: ApplicationCall): AuthorizedPrincipal {
//    val token = call.request.header("Authorization")?.removePrefix("Bearer ") ?: throw UnauthorizedException()
//    val user = tokenService.validateAndGetUser(token)
//    val session = DefaultSession()
//    return DefaultAuthorizedPrincipal(
//        userId = user.id,
//        username = user.username,
//        email = user.email,
//        userGroupId = user.groupId,
//        roles = user.roles,
//        session = session
//    )
//}
//inline fun <reified T : Any> Route.authenticatedPost(
//    path: String,
//    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
//) {
//    post(path) {
//        val principal = resolvePrincipal(call)
//        val session = principal.hostSession // or createSession()
//
//        withAuthenticatedSession(principal, session) {
//            val model = call.receive<T>()
//            handler(model)
//        }
//    }
//}
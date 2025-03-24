package po.auth.sessions.models

import po.auth.authentication.interfaces.AuthenticationPrincipal


class AuthorizedPrincipal(
    override val userId: Long = 0L,
    override val username: String = "no_name",
    override val email: String = "nomail@undeliverable.void",
    override val userGroupId: Long = 0L,
    override val roles: Set<String> = emptySet(),
) : AuthenticationPrincipal {


    fun copyReinit(src: AuthenticationPrincipal):AuthorizedPrincipal{
       return AuthorizedPrincipal(src.userId, src.username, src.email, src.userGroupId, src.roles)
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
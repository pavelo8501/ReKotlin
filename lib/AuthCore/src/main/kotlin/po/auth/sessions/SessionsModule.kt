package po.auth.sessions

import kotlin.coroutines.AbstractCoroutineContextElement

//
//class a : AbstractCoroutineContextElement
//
//
//suspend fun withAuthenticatedSession(
//    principal: AuthorizedPrincipal,
//    session: EmmitableSession,
//    block: suspend CoroutineScope.() -> Unit
//) {
//    withContext(session + principal, block)
//}
//
//// -- 2. Auth Resolver --
//suspend fun resolvePrincipal(call: ApplicationCall): AuthorizedPrincipal {
//    val token = call.request.header("Authorization")?.removePrefix("Bearer ")
//        ?: throw UnauthorizedException("Missing Authorization header")
//
//    val user = tokenService.validate(token) // <- your JWT/DB lookup here
//
//    val session = DefaultSession() // or built via factory
//    return DefaultAuthorizedPrincipal(
//        userId = user.id,
//        username = user.username,
//        email = user.email,
//        userGroupId = user.groupId,
//        roles = user.roles,
//        session = session
//    )
//}
//
//// -- 3. DSL: authenticatedPost<T> --
//inline fun <reified T : Any> Route.authenticatedPost(
//    path: String,
//    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
//) {
//    post(path) {
//        val principal = resolvePrincipal(call)
//        val session = principal.hostSession
//
//        withAuthenticatedSession(principal, session) {
//            val dto = call.receive<T>()
//            handler(dto)
//        }
//    }
//}
//
//// -- 4. Optional Error Type --
//class UnauthorizedException(message: String) : RuntimeException(message)
//
//interface AuthorizedPrincipal : CoroutineContext.Element {
//    val userId: Long
//    val username: String
//    val email: String
//    val userGroupId: Long
//    val roles: Set<String>
//    val hostSession: EmmitableSession
//    override val key: CoroutineContext.Key<*> get() = AuthorizedPrincipalKey
//}
//object AuthorizedPrincipalKey : CoroutineContext.Key<AuthorizedPrincipal>
//
//interface EmmitableSession : CoroutineContext.Element {
//    val sessionId: String
//    override val key: CoroutineContext.Key<*> get() = SessionKey
//}
//object SessionKey : CoroutineContext.Key<EmmitableSession>
//
//
//suspend fun withAuthenticatedSession(
//    principal: AuthorizedPrincipal,
//    session: EmmitableSession,
//    block: suspend CoroutineScope.() -> Unit
//) {
//    withContext(session + principal, block)
//}
//suspend fun resolvePrincipal(call: ApplicationCall): AuthorizedPrincipal {
//    val token = call.request.header("Authorization")?.removePrefix("Bearer ")
//        ?: throw UnauthorizedException("Missing Authorization header")
//
//    val user = tokenService.validate(token) // <- your JWT/DB logic
//    val session = DefaultSession()
//
//    return DefaultAuthorizedPrincipal(
//        userId = user.id,
//        username = user.username,
//        email = user.email,
//        userGroupId = user.groupId,
//        roles = user.roles,
//        session = session
//    )
//}
//inline fun <reified In : Any, reified Out : Any> Route.authenticatedPost(
//    path: String,
//    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(In) -> Out
//) {
//    post(path) {
//        val principal = resolvePrincipal(call)
//        val session = principal.hostSession
//
//        withAuthenticatedSession(principal, session) {
//            val dto = call.receive<In>()
//            val result = handler(dto)
//            call.respond(result)
//        }
//    }
//}
//class UnauthorizedException(message: String) : RuntimeException(message)

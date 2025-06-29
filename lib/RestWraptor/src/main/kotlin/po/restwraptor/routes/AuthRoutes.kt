package po.restwraptor.routes

import io.ktor.http.HttpHeaders
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import po.auth.exceptions.AuthErrorCode
import po.auth.exceptions.AuthException
import po.auth.exceptions.authException
import po.auth.extensions.authenticate
import po.auth.sessions.models.AuthorizedSession
import po.misc.types.getOrThrow
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.asBearer
import po.restwraptor.extensions.authSessionOrNull
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.scope.AuthConfigContext

fun Routing.configureAuthRoutes(authPrefix: String,  authConfigContext: AuthConfigContext) {
    val personalName = "AuthRoutes"
    val loginRoute = "login"
    post(withBaseUrl(authPrefix, loginRoute)) {

        val session = call.authSessionOrNull()
            .getOrThrow<AuthorizedSession, AuthException>(null){
                authException("Session can not be located", AuthErrorCode.SESSION_NOT_FOUND)
            }
        val credentials = call.receive<LoginRequest>()
        val principal = session.authenticate(credentials.login, credentials.password)
        val jwtToken = session.authenticator.jwtService.generateToken(principal, session)
        call.response.header(HttpHeaders.Authorization, jwtToken.token.asBearer())
        call.response.header(WraptorHeaders.XAuthToken.value, session.sessionID)

        call.respond(ApiResponse(jwtToken.token))

    }
}
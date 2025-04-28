package po.restwraptor.routes

import io.ktor.http.HttpHeaders
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.serialization.SerializationException
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.extensions.authenticate
import po.lognotify.extensions.startTask
import po.misc.exceptions.HandlerType
import po.misc.exceptions.getOrException
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.asBearer
import po.restwraptor.extensions.authSessionOrNull
import po.restwraptor.extensions.respondBadRequest
import po.restwraptor.extensions.respondInternal
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.request.LogoutRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.scope.AuthConfigContext
import java.lang.Exception

fun Routing.configureAuthRoutes(authPrefix: String,  authConfigContext: AuthConfigContext) {
    val personalName = "AuthRoutes"

    val loginRoute = "login"

    post(withBaseUrl(authPrefix, loginRoute)) {

        // call.withSession {
        startTask("Process Post login", call.coroutineContext, "$personalName $loginRoute") { handler ->
            call.authSessionOrNull().getOrException {
                AuthException("Session can not be located", ErrorCodes.SESSION_NOT_FOUND,)
            }.let { session ->
                handler.handleFailure(HandlerType.SKIP_SELF){ throwable ->
                    println("Error reached")
                    when (throwable) {
                        is AuthException -> {
                            if (throwable.code.value >= 4000 && throwable.code.value < 5000) {
                                respondUnauthorized(throwable.message, throwable.code.value)
                            } else {
                                respondInternal(throwable.message, throwable.code.value)
                            }
                        }
                        else -> {
                            respondInternal(throwable)
                        }
                    }
                    respondInternal(throwable)
                }
                val credentials = call.receive<LoginRequest>()
                val principal = session.authenticate(credentials.login, credentials.password)
                val jwtToken = session.authenticator.jwtService.generateToken(principal, session)
                call.response.header(HttpHeaders.Authorization, jwtToken.token.asBearer())
                call.response.header(WraptorHeaders.XAuthToken.value, session.sessionID)

                handler.info("Header ${HttpHeaders.Authorization} set value: ${jwtToken.token.asBearer()}")
                handler.info("Header ${WraptorHeaders.XAuthToken.value} set value: ${session.sessionID}")
                call.respond(ApiResponse(jwtToken.token))
            }
        }.resultOrException()
    }

    post(withBaseUrl(authPrefix, "refresh")) {
//        val authHeader = call.request.headers["Authorization"]
//        if (authHeader == null || !authHeader.startsWith("Bearer")) {
//            respondUnauthorized("Missing or invalid token")
//            return@post
//        }
//        jwtService.checkExpiration(authHeader) { token ->
//            if (token == null) {
//                respondUnauthorized("Invalid token data")
//            } else {
//                call.response.header("Authorization", "Bearer $token")
//                call.respond(HttpStatusCode.OK, ApiResponse(token))
//            }
//        }
    }

    post(withBaseUrl(authPrefix, "logout")) {
        val request = call.receive<ApiRequest<LogoutRequest>>()
        call.respond(ApiResponse(true))
    }
}
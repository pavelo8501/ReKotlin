package po.restwraptor.routes

import io.ktor.http.HttpHeaders
import po.auth.authentication.extensions.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.serialization.SerializationException
import po.auth.AuthSessionManager
import po.auth.authentication.Authenticator
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.extensions.asBearer
import po.lognotify.extensions.startTask
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.extensions.getOrConfigurationEx
import po.restwraptor.extensions.respondBadRequest
import po.restwraptor.extensions.respondInternal
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.withBaseUrl
import po.restwraptor.extensions.withSession
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.request.LogoutRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.scope.AuthConfigContext

fun Routing.configureAuthRoutes(authPrefix: String,  authConfigContext: AuthConfigContext) {
    val personalName = "AuthRoutes"

    //val jwtService = AuthSessionManager.jwtService.getOrConfigurationEx("JWTService undefined", ExceptionCodes.AUTH_SERVICE_UNDEFINED)

    post(withBaseUrl(authPrefix, "login")) {

        call.withSession {
            startTask("Process Post login", call.coroutineContext, personalName) {handler->
                val credentials = call.receive<LoginRequest>()

                val principal = authenticate(credentials.login, credentials.password)
                val jwtToken =  authenticator.jwtService.generateToken(principal, sessionId)

                call.response.header(HttpHeaders.Authorization, jwtToken.token.asBearer())
                call.response.header(WraptorHeaders.XAuthToken.value, sessionId)

                handler.info("Header ${HttpHeaders.Authorization} set value: ${jwtToken.token.asBearer()}")
                handler.info("Header ${WraptorHeaders.XAuthToken.value} set value: ${sessionId}")
                call.respond(ApiResponse(jwtToken.token))

            }.onFail { throwable ->
                when (throwable) {
                    is AuthException -> respondUnauthorized("Authorization failed for credentials supplied")
                    is SerializationException -> respondBadRequest(throwable.message.toString())
                    else -> {
                        respondInternal(throwable)
                    }
                }
            }
        }
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
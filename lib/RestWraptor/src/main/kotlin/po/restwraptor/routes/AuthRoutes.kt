package po.restwraptor.routes

import io.ktor.http.HttpStatusCode
import  po.auth.authentication.extensions.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.extensions.getOrConfigurationEx
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.toUrl
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.request.LogoutRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.scope.AuthConfigContext

fun Routing.configureAuthRoutes(baseURL: String,  authConfigContext: AuthConfigContext) {

    val jwtService = authConfigContext.jwtService.getOrConfigurationEx("JWTService undefined", ExceptionCodes.AUTH_SERVICE_UNDEFINED)
    val loginUrl = toUrl(baseURL, "login")
    post(loginUrl) {
        val credentials = call.receive<LoginRequest>()
        authenticate(credentials.login, credentials.password)
    }

    val refreshUrl = toUrl(baseURL, "refresh")
    post(refreshUrl) {
        val authHeader = call.request.headers["Authorization"]
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            respondUnauthorized("Missing or invalid token")
            return@post
        }
        jwtService.checkExpiration(authHeader) { token ->
            if (token == null) {
                respondUnauthorized("Invalid token data")
            } else {
                call.response.header("Authorization", "Bearer $token")
                call.respond(HttpStatusCode.OK, ApiResponse(token))
            }
        }
    }

    val logoutUrl = toUrl(baseURL, "logout")
    post(logoutUrl) {
        val request = call.receive<ApiRequest<LogoutRequest>>()
        call.respond(ApiResponse(true))
    }
}
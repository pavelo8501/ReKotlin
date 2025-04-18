package po.restwraptor.routes

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import po.auth.authentication.extensions.validateCredentials
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.extensions.toUrl
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.request.LogoutRequest
import po.restwraptor.models.response.ApiResponse

fun configureAuthRoutes(routing: Routing, baseURL: String){

    routing.apply {
        val loginUrl =  toUrl(baseURL, "login")
        post(loginUrl){
            val credentials = call.receive<LoginRequest>()
            validateCredentials(credentials.login, credentials.password)

        }

        val refreshUrl =  toUrl(baseURL, "refresh")
        post(refreshUrl) {
            val authHeader = call.request.headers["Authorization"]
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                respondUnauthorized("Missing or invalid token")
                return@post
            }
//                    try {
//                        jwtService?.let { service ->
//                            service.checkExpiration(authHeader){token->
//                                if(token == null){
//                                    respondUnauthorized("Invalid token data")
//                                }else{
//                                    call.response.header("Authorization", "Bearer $token")
//                                    call.respond(HttpStatusCode.OK, ApiResponse(token))
//                                }
//                            }
//                        }
//                    } catch (ex: AuthException) {
//                        respondInternal(ex)
//                        respondUnauthorized("Token expired or invalid: ${ex.message}")
//                    }
                }

        val logoutUrl =  toUrl(baseURL, "logout")
        post(logoutUrl) {
            val request =  call.receive<ApiRequest<LogoutRequest>>()
            call.respond(ApiResponse(true))
        }

    }
}
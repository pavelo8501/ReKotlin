package po.playground.projects.rest_service

import po.api.rest_service.server.*
import po.api.rest_service.security.JwtConfig
import po.api.rest_service.security.JWTService


import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun startApiServer(host: String, port: Int) {

    val jwtConfig = JwtConfig(
        realm = "Secure api access",
        audience = "audience",
        issuer = "issuer",
        secret = "secret",
        privateKeyString = "privateKey",
        publicKeyString = "publicKey"
    )

    val tokenService = JWTService.configure(jwtConfig)


    val apiServer = ApiServer()
        .configureHost(host, port)
        .configureModule {
            configureErrorHandling()
            routing {
                post("/login") {
                    val parameters = call.receiveParameters()
                    val username = parameters["username"] ?: ""
                    val password = parameters["password"] ?: ""
                    // Authenticate user (replace with real authentication logic)
                    if (username == "user" && password == "pass") {
                        val token = tokenService.generateToken(username, "payload")
                        call.respond(mapOf("token" to token))
                    } else {
                        throw AuthenticationException("Invalid credentials")
                    }
                }

                authenticate("auth-jwt") {
                    get("/secure-endpoint") {
                        val principal = call.principl<JWTPrincipal>()
                        val username = principal!!.payload.getClaim("username").asString()
                        call.respondText("Hello, $username")
                    }
                }

                get("/public-endpoint") {
                    call.respondText("This is a public endpoint.")
                }
            }
        }
        .start(wait = true)

}
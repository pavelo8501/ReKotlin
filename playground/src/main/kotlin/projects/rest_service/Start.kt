package po.playground.projects.rest_service

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import po.api.rest_service.server.*
import po.api.rest_service.security.JwtConfig
import po.api.rest_service.security.JWTService

import io.ktor.server.routing.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.request.*

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*

import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.*
import io.ktor.server.routing.*

import java.io.File
import java.nio.file.Paths

import po.playground.projects.rest_service.users.User

fun startApiServer(host: String, port: Int) {

    val currentDir = File("").absolutePath

    val jwtConfig = JwtConfig(
        realm = "Secure api access",
        audience = "audience",
        issuer = "http://127.0.0.1:8080",
    )

    jwtConfig.setKeys(
        publicKeyString =   File(currentDir+File.separator+"keys"+File.separator+"ktor.spki").readText(),
        privateKeyString =  File(currentDir+File.separator+"keys"+File.separator+"ktor.pk8").readText()
    )
    val tokenService = JWTService.configure(jwtConfig)

    val apiServer = ApiServer(){

        install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtConfig.realm
                verifier(tokenService.getVerifier())
                validate { credential ->
                    if (credential.payload.audience.contains(jwtConfig.audience)) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }
        configureErrorHandling()

        routing {
            get("/.well-known/jwks.json") {
                val appPath = Paths.get("").toAbsolutePath().toString()
                val jwksContent = File(appPath+File.separator+"certs"+File.separator+"jwks.json").readText()
                call.respondText(jwksContent, ContentType.Application.Json)
            }
            route("/api/login") {
                get {
                    call.respondText("Hello :) Better use POST")
                }
                post {
                    try {
                        val parameters = call.receiveParameters()
                        val username = parameters["username"] ?: ""
                        val password = parameters["password"] ?: ""

                        //!!! Remember to substitute this with a real authentication in production
                        val user =  if (username == "user" && password == "pass") {
                            User(username, password).also {
                                it.id = 1
                                it.email = "some@mail.com"
                            }
                        }else {
                            throw AuthenticationException("Invalid credentials")
                        }
                        val token = tokenService.generateToken(user)

                        call.respond(hashMapOf("token" to token))
                    }catch (e: AuthenticationException){
                        call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
                    }
                    catch (e: Exception){
                        call.respondText(e.message?:"Internal server error", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            authenticate("auth-jwt") {
                get("/secure-endpoint") {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal!!.payload.getClaim("username").asString()
                    call.respondText("Hello, $username")
                }
            }

            get("/public-endpoint") {
                call.respondText("This is a public endpoint.")
            }
        }
    }
    apiServer.configureHost(host, port)
    apiServer.start()

}
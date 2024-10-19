package po.playground.projects.rest_service

import io.ktor.http.ContentType
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
import po.playground.projects.rest_service.users.User
import java.io.File
import java.nio.file.Paths

fun startApiServer(host: String, port: Int) {

    val currentDir = File("").absolutePath

    val a = 10

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

    val apiServer = ApiServer()
        .configureHost(host, port)
        .configureModule {
            configureErrorHandling()

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

            routing {
                get("/.well-known/jwks.json") {
                    val appPath = Paths.get("").toAbsolutePath().toString()
                    val jwksContent = File(appPath+File.separator+"certs"+File.separator+"jwks.json").readText()
                    call.respondText(jwksContent, ContentType.Application.Json)
                }
                post("/login") {
                    val parameters = call.receiveParameters()
                    val username = parameters["username"] ?: ""
                    val password = parameters["password"] ?: ""

                    if (username == "user" && password == "pass") {
                        val fakeUser = User(username, password).also {
                            it.id = 1
                            it.email = "some@mail.com"
                        }
                        val token = tokenService.generateToken(fakeUser)
                        call.respond(mapOf("token" to token))
                    } else {
                        throw AuthenticationException("Invalid credentials")
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
        .start(wait = true)

}
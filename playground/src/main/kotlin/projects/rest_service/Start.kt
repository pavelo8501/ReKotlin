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

import io.ktor.server.plugins.CannotTransformContentToTypeException
import po.api.rest_service.exceptions.DataErrorCodes
import po.api.rest_service.exceptions.DataException
import po.api.rest_service.logger.LogFunction
import po.api.rest_service.logger.LogLevel
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.LoginRequestData
import po.api.rest_service.models.RequestData

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

    val customLogFunction: LogFunction = { message, level, date, throwable ->
        // User's custom logging logic
        // For example, using SLF4J:
        val logger = org.slf4j.LoggerFactory.getLogger("ApiServerLogger")
        when (level) {
            LogLevel.MESSAGE -> logger.debug("[{}] {}", date, message)
            LogLevel.ACTION -> logger.info("[$date] $message")
            LogLevel.WARNING -> logger.warn("[$date] $message")
            LogLevel.EXCEPTION -> logger.error("[$date] $message", throwable)
        }
    }

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
                    val jwksContent = File(appPath + File.separator + "certs" + File.separator + "jwks.json").readText()
                    call.respondText(jwksContent, ContentType.Application.Json)
                }
                route("/api/login") {
                    get {
                        call.respondText("Hello :) Better use POST")
                    }
                    post {
                        try {
                            apiLogger.info("Request content type: ${call.request.contentType()}")
                            val loginRequest = call.receive<ApiRequest<RequestData>>()
                            if(loginRequest.data !is LoginRequestData){
                                throw DataException(DataErrorCodes.REQUEST_DATA_MISMATCH,"Not a login request")
                            }
                            (loginRequest.data as LoginRequestData).let { loginData ->
                                //!!! Remember to substitute this with a real authentication in production
                                val user = if (loginData.value.username == "user" && loginData.value.password == "pass") {
                                    User(loginData.value.username, loginData.value.password).also {
                                        it.id = 1
                                        it.email = "some@mail.com"
                                    }
                                } else {
                                    throw AuthenticationException("Invalid credentials")
                                }
                                val token = tokenService.generateToken(user)
                                call.respond(hashMapOf("token" to token))
                            }
                        } catch (e: AuthenticationException) {
                            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
                        } catch (e: CannotTransformContentToTypeException) {
                            println(e.message)
                            call.respondText("Request data cannot be deserialized", status = HttpStatusCode.NotAcceptable)
                        }
                        catch (e: Exception) {
                            val message = e.message ?: "Internal server error"
                            println(message)
                            call.respondText("Internal server error", status = HttpStatusCode.InternalServerError)
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
    apiServer.apiLogger.registerLogFunction(LogLevel.MESSAGE, customLogFunction)
    apiServer.configureHost(host, port)
    apiServer.start()

}
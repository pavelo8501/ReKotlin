package po.playground.projects.rest_service

import io.ktor.http.ContentType
import po.api.rest_service.server.*

import io.ktor.server.routing.*
import io.ktor.server.response.*

import po.api.rest_service.RestServer
import po.api.rest_service.apiLogger
import po.api.rest_service.logger.LogFunction
import po.api.rest_service.logger.LogLevel

import java.io.File
import java.nio.file.Paths


fun startApiServer(host: String, port: Int) {

    val currentDir = File("").absolutePath

//    val jwtConfig = JwtConfig(
//        realm = "Secure api access",
//        audience = "audience",
//        issuer = "http://127.0.0.1:8080",
//    )

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

    RestServer.apiConfig.setAuthKeys(
        publicKey  =  File(currentDir+File.separator+"keys"+File.separator+"ktor.spki").readText(),
        privateKey =  File(currentDir+File.separator+"keys"+File.separator+"ktor.pk8").readText()
    )

    val apiServer = RestServer() {

        apiLogger.registerLogFunction(LogLevel.MESSAGE, customLogFunction)

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
            }
//            authenticate("auth-jwt") {
//                get("/api/secure-endpoint") {
//                    val principal = call.principal<JWTPrincipal>()
//                    val username = principal!!.payload.getClaim("username").asString()
//                    call.respond(ApiResponse("Hello, $username"))
//                }
//            }

            get("/public-endpoint") {
                call.respondText("This is a public endpoint.")
            }
        }
    }.apply {
        configureHost(host, port)
    }
    apiServer.start()
}
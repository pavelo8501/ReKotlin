package po.playground.projects.rest_service

import io.ktor.http.ContentType
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import po.api.rest_service.server.*

import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

import po.api.rest_service.RestServer
import po.api.rest_service.apiLogger
import po.api.rest_service.common.ApiEntity
import po.api.rest_service.logger.LogFunction
import po.api.rest_service.logger.LogLevel
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.ApiResponse
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.CreateRequestData

import java.io.File
import java.nio.file.Paths

@Serializable
sealed class TestPartner(): ApiEntity{

    override val id: Long = 0

    val name: String = ""
    val nr: Int = 0

}

fun startApiServer(host: String, port: Int) {

    val currentDir = File("").absolutePath

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

            authenticate("auth-jwt") {
                get("/api/secure-endpoint") {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal!!.payload.getClaim("username").asString()
                    call.respond(ApiResponse("Hello, $username"))
                }
            }

            post("/api/partners") {

             val partner =  call.receive<ApiRequest<TestPartner>>()
             val a =10

            }

            get("/public-endpoint") {
                call.respond(ApiResponse("Public endpoint"))
            }
        }
    }.apply {
        configureHost(host, port)
    }
    apiServer.start()
}
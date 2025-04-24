package po.restwraptor.plugins

import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.path
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtToken
import po.lognotify.extensions.startTask
import po.misc.exceptions.getOrException
import po.misc.exceptions.getOrThrow
import po.restwraptor.enums.WraptorHeaders
import po.restwraptor.extensions.getWraptorRoutes
import po.restwraptor.extensions.respondUnauthorized
import po.restwraptor.models.server.WraptorRoute

class JWTPluginConfig{

    var headerName: String = "X-Auth"
    lateinit var key: String
    lateinit var service: JWTService

    fun setup(key: String, service: JWTService) {
        this.key = key
        this.service = service
    }
}

val JWTPlugin: ApplicationPlugin<JWTPluginConfig> = createApplicationPlugin(
    name = "CallInterceptorPlugin",
    createConfiguration =  ::JWTPluginConfig
){

    val headerName = pluginConfig.headerName
    val service = pluginConfig.service
    val key = pluginConfig.key

    val securedRoutes : MutableList<WraptorRoute> = mutableListOf()
    fun checkDestinationSecured(path: String): Boolean{
       return securedRoutes.any { it.path.lowercase() == path.lowercase() }
    }

    fun readSessionId(headers: Headers): String?{
        return headers[WraptorHeaders.XAuthToken.value]
    }

    application.install(Authentication) {
        jwt(key) {
            realm = service.realm
            verifier(service.getVerifier())
            validate { credential ->
                val call = this
                val path = call.request.path()
                service.validateToken(credential)
            }
        }
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        securedRoutes.addAll(application.getWraptorRoutes().filter { it.isSecured })
    }

    on(CallSetup) { call ->
        startTask("Processing incoming call", call.coroutineContext, "JWTPlugin") { handler ->
            val path = call.request.path()
            handler.info("Processing call to $path")

            if (checkDestinationSecured(path)) {
                val sessionId = readSessionId(call.request.headers).getOrThrow<String, AuthException>()
                val jwtToken = service.tokenRepository.resolve(sessionId)
                    .getOrThrow<JwtToken, AuthException>("Token not found in repository")
                val validatedToken = service.isNotExpired(jwtToken) {
                    handler.info("Invalidating Token due to expiry")
                    service.tokenRepository.invalidate(jwtToken.sessionId)
                    call.respondUnauthorized("Session expired", HttpStatusCode.Unauthorized.value)
                    return@isNotExpired
                }
                handler.info("Token is valid, call allowed to proceed")
                validatedToken
            }
        }
    }
}
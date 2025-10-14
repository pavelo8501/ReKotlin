package po.restwraptor.plugins

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
import po.auth.exceptions.AuthException
import po.auth.authentication.jwt.JWTService
import po.auth.authentication.jwt.models.JwtToken
import po.auth.exceptions.authException
import po.misc.types.getOrManaged
import po.misc.types.getOrThrow
import po.restwraptor.RestWraptorServer
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
        application.getWraptorRoutes(RestWraptorServer){list->
            securedRoutes.addAll(list.filter { it.isSecured })
        }
    }

    on(CallSetup) { call ->
        val path = call.request.path()

        if (checkDestinationSecured(path)) {
            val sessionId = readSessionId(call.request.headers).getOrManaged("SessionId")
            val jwtToken = service.tokenRepository.resolve(sessionId).getOrManaged("Token not found in repository")
            val validatedToken = service.isNotExpired(jwtToken) {
                service.tokenRepository.invalidate(jwtToken.sessionId)
                return@isNotExpired
            }
            validatedToken
        }
    }
}
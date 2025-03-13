package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import po.restwraptor.security.JWTService

val ReplyInterceptorPlugin = createApplicationPlugin(
    name = "CallInterceptorPlugin",
    createConfiguration =  ::PluginConfiguration
    ) {

    pluginConfig.apply {

        onCall { call ->
            val uri = call.request.uri
            if (uri.endsWith("/") && uri != "/") {
                call.respondRedirect(uri.removeSuffix("/"))
                return@onCall
            }
        }

//        on(ResponseSent) { call ->
//
//        }
    }
}

class PluginConfiguration {
    var service: JWTService? = null

    fun injectService(jwtService: JWTService){
        service = jwtService
    }
}
package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.MonitoringEvent
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
            try{
                val headers = call.request.headers
                headers.forEach { name, values ->  println("${name} : ${values}") }
                val uri = call.request.uri
                if (uri.endsWith("/") && uri != "/") {
                    call.respondRedirect(uri.removeSuffix("/"))
                    return@onCall
                }
            } catch (e: Exception) {
                val a = e.message
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
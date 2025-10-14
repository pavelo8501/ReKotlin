package po.restwraptor.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.auth.AuthenticationChecked
import po.misc.context.TraceableContext
import po.restwraptor.RestWraptorServer
import po.restwraptor.extensions.getWraptorRoutes
import po.restwraptor.models.server.WraptorRoute

class CoreAuthRoutePluginConf {

    var headerName: String = HttpHeaders.Authorization
    var pluginKey : String = "jwt-auth-route"
}

class WraptorContext(): TraceableContext

val CoreAuthRoutePlugin = createRouteScopedPlugin(
    name = "CoreAuthRoutePlugin",
    createConfiguration =  ::CoreAuthRoutePluginConf
) {

    val headerName = pluginConfig.headerName
    val pluginKey = pluginConfig.pluginKey

    val securedRoutes: MutableList<WraptorRoute> = mutableListOf()

    fun checkDestinationSecured(path: String): Boolean {
        return securedRoutes.any { it.path.lowercase() == path.lowercase() }
    }

    on(AuthenticationChecked) { call ->
       // resolveSessionFromHeader(call)
    }

    onCall{call->
       //  resolveSessionFromHeader(call)
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.getWraptorRoutes(RestWraptorServer){
            securedRoutes.addAll(it.filter {it.isSecured })
        }
    }

}
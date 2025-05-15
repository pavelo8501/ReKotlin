package po.restwraptor.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import po.lognotify.extensions.newTask
import po.restwraptor.extensions.getWraptorRoutes
import po.restwraptor.extensions.resolveSessionFromHeader
import po.restwraptor.extensions.sessionToAttributes
import po.restwraptor.models.server.WraptorRoute


class CoreAuthPluginConfiguration {
    var headerName : String = HttpHeaders.Authorization
    var pluginKey: String = "jwt-auth"
}

val CoreAuthApplicationPlugin = createApplicationPlugin(
    name = "CoreAuthApplicationPlugin",
    createConfiguration =  ::CoreAuthPluginConfiguration

){

    val headerName = pluginConfig.headerName
    val pluginKey =  pluginConfig.pluginKey

    val securedRoutes : MutableList<WraptorRoute> = mutableListOf()

    fun checkDestinationSecured(path: String): Boolean{
        return securedRoutes.any { it.path.lowercase() == path.lowercase() }
    }

    onCall{call->
        newTask("Processing incoming call", call.coroutineContext, "CoreAuthApplicationPlugin"){handler->
            val session = resolveSessionFromHeader(call)
            handler.info("Session created ${session.sessionID}")
            call.sessionToAttributes(session)
        }
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.getWraptorRoutes(){
            securedRoutes.addAll(it.filter { it.isSecured  })
        }
    }


}
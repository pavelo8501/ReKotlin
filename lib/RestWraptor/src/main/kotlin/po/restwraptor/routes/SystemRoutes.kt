package po.restwraptor.routes

import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.route
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.extensions.respondNotFound
import po.restwraptor.interfaces.WraptorResponse
import po.restwraptor.scope.ConfigContext

fun Routing.configureSystemRoutes(configContext: ConfigContext, responseProvider:()-> WraptorResponse<*>) {

    val wraptor = configContext.wraptor
    
    options(withBaseUrl("status")) {
        call.response.header("Access-Control-Allow-Origin", "*")
        call.respondText("OK")
    }
    get(withBaseUrl("status")) {
        call.respond(wraptor.status().toString())
    }

    get(withBaseUrl("status-json")) {
        val responseStatus: String = "OK"
        call.respond(responseProvider)
    }

    route("{...}") {
        handle {
            if (configContext.apiConfig.environment != EnvironmentType.PROD) {
                call.respondNotFound(
                    wraptor.getRoutes().map { ("Path: ${it.path}  Method: ${it.selector} IsSecured: ${it.isSecured} ") },
                    responseProvider
                )
            } else {
                call.respondNotFound(listOf("Oops! This path does not exist."), responseProvider)

            }
        }
    }
}
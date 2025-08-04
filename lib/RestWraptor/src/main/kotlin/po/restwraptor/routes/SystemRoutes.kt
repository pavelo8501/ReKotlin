package po.restwraptor.routes

import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.route
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.extensions.toUrl
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.scope.ConfigContext
import po.restwraptor.extensions.respondNotFound

fun Routing.configureSystemRoutes(baseURL: String, configContext: ConfigContext) {

    val wraptor = configContext.wraptor

    val statusUrl = toUrl(baseURL, "status")
    options(statusUrl) {
        call.response.header("Access-Control-Allow-Origin", "*")
        call.respondText("OK")
    }
    get(statusUrl) {
        call.respond(wraptor.status().toString())
    }
    val statusJsonUrl = toUrl(baseURL, "status-json")
    get(statusJsonUrl) {
        val responseStatus: String = "OK"
        call.respond(ApiResponse(responseStatus))
    }
    route("{...}") {
        handle {
            if (configContext.apiConfig.environment != EnvironmentType.PROD) {
                call.respondNotFound(
                    wraptor.getRoutes().map { ("Path: ${it.path}  Method: ${it.selector} IsSecured: ${it.isSecured} ") }
                )
            } else {
                call.respondNotFound(listOf("Oops! This path does not exist."))
            }
        }
    }
}
package po.restwraptor.models.server

import io.ktor.server.routing.RoutingNode

data class WraptorRoute(
    private val node : RoutingNode,
    val selector : String,
    val path : String,
    val isSecured : Boolean = false,
)
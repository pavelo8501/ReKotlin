package po.restwraptor.models.server


import io.ktor.server.routing.RoutingNode
import po.restwraptor.enums.RouteSelector

data class WraptorRoute(
    val selector : RouteSelector,
    val path : String,
    val isSecured : Boolean = false,
    private val node : RoutingNode,
)
package po.restwraptor.routes


import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.restwraptor.scope.ConfigContext


fun  ConfigContext.buildManagedRoutes(
    routeGroupPath: String? = null,
    builder:ManagedRoute.()-> Unit
){
    application.routing {
        ManagedRoute(this, routeGroupPath).builder()
    }
}

class ManagedRoute(
    private val routing: Routing,
    private val routeGroupPath: String? = null
): TasksManaged {

    override val identity: CTXIdentity<ManagedRoute> = asIdentity()

     fun managedGet(vararg pathParts:String, body: suspend RoutingContext.() -> Unit){
        val result = partsToUrl(pathParts.toList(),  routeGroupPath)
        routing.get(result, body)
    }

    fun managedPost(vararg pathParts:String, body: suspend RoutingContext.() -> Unit){
        val result = partsToUrl(pathParts.toList(),  routeGroupPath)
        routing.post(result, body)
    }

    fun managedWrongPath(body: suspend RoutingContext.() -> Unit){
        routing.post("{...}", body)
    }
}
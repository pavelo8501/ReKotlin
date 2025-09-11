package po.restwraptor.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import po.auth.getOrNewSession
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.containers.LazyContainer
import po.misc.containers.lazyContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.restwraptor.calls.callerInfo
import po.restwraptor.session.sessionFromAttributes



class ManagedRouting(){
    val routes = mutableListOf<ManagedRoute>()
    internal fun provideRouteKtorRouting(routing:  Routing){
        routes.forEach {
            it.routing.provideValue(routing)
        }
    }

    fun managedRoutes(routeGroupPath: String,  block:ManagedRoute.()-> Unit){
       val managed =  ManagedRoute(routeGroupPath)
        managed.block()
        routes.add(managed)
    }

    fun managedRoutes(block:ManagedRoute.()-> Unit){
        val managed =  ManagedRoute(null)
        managed.block()
        routes.add(managed)
    }

}


class ManagedRoute(
    private val routeGroupPath: String? = null
): TasksManaged {


    internal val routing: LazyContainer<Routing> = lazyContainerOf()

    override val identity: CTXIdentity<ManagedRoute> = asIdentity()

     fun managedGet(vararg pathParts:String, body: suspend RoutingContext.(AuthorizedSession) -> Unit){

        val result = partsToUrl(pathParts.toList(),  routeGroupPath)

         routing.requestValue(this){routing->

             routing.get(result){
                 val session =  call.sessionFromAttributes()?.let {
                     "Using existent session".output(Colour.GREEN)
                     it.output()
                     it
                 }?:run {
                     "ReCreating session".output(Colour.CYAN)
                     getOrNewSession(call.callerInfo())
                 }
                 body(this, session)
             }

         }
    }

    fun managedPost(vararg pathParts:String, body: suspend RoutingContext.(AuthorizedSession) -> Unit) {
        val result = partsToUrl(pathParts.toList(), routeGroupPath)

        routing.requestValue(this) { routing ->
            routing.post(result) {
                val session = call.sessionFromAttributes()?.let {
                    "Using existent".output(Colour.GREEN)
                    it.output()
                    it
                } ?: run {
                    "ReCreating session".output(Colour.CYAN)
                    getOrNewSession(call.callerInfo())
                }
                body(this, session)
            }
        }
    }

    fun managedWrongPath(body: suspend RoutingContext.(AuthorizedSession) -> Unit) {
        routing.requestValue(this) { routing ->
            routing.post("{...}", body)
        }
    }
}
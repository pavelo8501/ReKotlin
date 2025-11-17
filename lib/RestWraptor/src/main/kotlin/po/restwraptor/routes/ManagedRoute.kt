package po.restwraptor.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import po.auth.getOrNewSession
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.containers.lazy.LazyContainer
import po.misc.containers.lazy.lazyContainerOf
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

         routing.getValue(this){routing->

             routing.get(result){
                 val session =  call.sessionFromAttributes()?.let {
                     "Using existent session".output(Colour.Green)
                     it.output()
                     it
                 }?:run {
                     "ReCreating session".output(Colour.Cyan)
                     getOrNewSession(call.callerInfo())
                 }
                 body(this, session)
             }

         }
    }

    fun managedPost(vararg pathParts:String, body: suspend RoutingContext.(AuthorizedSession) -> Unit) {
        val result = partsToUrl(pathParts.toList(), routeGroupPath)

        routing.getValue(this) { routing ->
            routing.post(result) {
                val session = call.sessionFromAttributes()?.let {
                    "Using existent".output(Colour.Green)
                    it.output()
                    it
                } ?: run {
                    "ReCreating session".output(Colour.Cyan)
                    getOrNewSession(call.callerInfo())
                }
                body(this, session)
            }
        }
    }

    fun managedWrongPath(body: suspend RoutingContext.(AuthorizedSession) -> Unit) {
        routing.getValue(this) { routing ->
            routing.post("{...}", body)
        }
    }
}
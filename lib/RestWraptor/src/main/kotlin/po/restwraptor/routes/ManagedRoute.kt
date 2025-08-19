package po.restwraptor.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import po.auth.getOrNewSession
import po.auth.sessions.models.AuthorizedSession
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.restwraptor.calls.callerInfo
import po.restwraptor.scope.ConfigContext
import po.restwraptor.session.sessionFromAttributes


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

     fun managedGet(vararg pathParts:String, body: suspend RoutingContext.(AuthorizedSession) -> Unit){
        val result = partsToUrl(pathParts.toList(),  routeGroupPath)
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

    fun managedPost(vararg pathParts:String, body: suspend RoutingContext.(AuthorizedSession) -> Unit){
        val result = partsToUrl(pathParts.toList(),  routeGroupPath)

        routing.post(result){
            val session =  call.sessionFromAttributes()?.let {
                "Using existent".output(Colour.GREEN)
                it.output()
                it
            }?:run {
                "ReCreating session".output(Colour.CYAN)
                getOrNewSession(call.callerInfo())
            }
            body(this, session)
        }
    }

    fun managedWrongPath(body: suspend RoutingContext.(AuthorizedSession) -> Unit){
        routing.post("{...}", body)
    }
}
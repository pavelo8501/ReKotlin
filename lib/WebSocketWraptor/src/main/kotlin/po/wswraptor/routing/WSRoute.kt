package po.wswraptor.routing

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket


class WSResourceRoute(val resource: String, parent: WSRoute)

class WSRoute(
    val path: String,
    val resource: String,
    val options : String = "",
    val wsSession : DefaultWebSocketServerSession
){

    fun registerRoute(resourceRoute:WSResourceRoute){
        println(resourceRoute)
    }

    inline fun resourceSocket(resource: String, routeBuilderFn : WSResourceRoute.()->Unit ){
        val res =  WSResourceRoute(resource, this)
        res.routeBuilderFn()
        registerRoute(res)
    }
}
package po.restwraptor.scope

import io.ktor.server.application.Application
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.AuthenticationRouteSelector
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.RoutingNode
import io.ktor.server.routing.RoutingRoot
import po.restwraptor.RestWrapTor
import po.restwraptor.enums.RouteSelector
import po.restwraptor.models.server.WraptorRoute

class CoreContext(private val app : Application, private val wraptor: RestWrapTor) {

    val routes : List<WraptorRoute>  by  lazy { getAllRegisteredRoutes() }

    private fun cleanPathStr(path: String):String{
        return path.replace(Regex("\\(authenticate .*?\\)"), "").replace("//", "/").trimEnd('/')
    }

    private fun getAllRegisteredRoutes(): List<WraptorRoute> {
        val routes: MutableList<WraptorRoute> = mutableListOf()
        fun isRouteSecured(node: RoutingNode): Boolean {
            var current: RoutingNode? = node
            while (current != null) {
                if (current.selector is AuthenticationRouteSelector) {
                    return true
                }
                current = current.parent
            }
            return false
        }

        fun traverseChildren(node: RoutingNode, parentSecured: Boolean = false){
            var isSecured = parentSecured
            if(parentSecured == false){
                isSecured = isRouteSecured(node)
            }
            (node.selector as? HttpMethodRouteSelector)?.let {
                routes.add(WraptorRoute(RouteSelector.fromValue(it.method.value), cleanPathStr(node.parent.toString()), isSecured, node))
            }
            node.children.forEach { traverseChildren(it, isSecured) }
        }
        app.pluginOrNull(RoutingRoot)?.let {
            it.children.forEach {
                traverseChildren(it)
            } }?:run {
                print("No plugin")
        }
        return routes
    }

    fun getWraptorRoutes(): List<WraptorRoute>{
       return  getAllRegisteredRoutes()
    }

//    fun List<WraptorRoute>.toList(){
//        val  result = mutableListOf<String>()
//        val unsecured = this.filter { it.isSecured == false }
//        val secured = this.filter { it.isSecured == true }
//
//        result.add("This server defines ${unsecured.count()} public endpoint and ${secured.count()} secure ")
//        wraptor.connectors.forEach {connector->
//            unsecured.forEach {result.add("Endpoint ${connector}${it.path} ${it.selector}  isSecured:${it.isSecured}") }
//        }
//
//    }

}
package po.restwraptor.extensions

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import po.restwraptor.exceptions.ExceptionCodes


private fun partsToUrl(pathParts: List<String>):  String {
    val result =  pathParts
        .asSequence()
        .map { it.trim().trim('/') }
        .filter { it.isNotEmpty() }
        .joinToString("/")
    return  result
}


fun Routing.baseApi():String{
    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig().baseApiRoute
    }
    return ""
}

fun Route.toUrl(vararg pathParts:String ):  String = partsToUrl(pathParts.toList())
fun Route.withBaseUrl(vararg pathParts:String ): String{

    val list = mutableListOf<String>(application.rootPath)
    list.addAll(pathParts)
    return partsToUrl((list))
}

fun Routing.jwtSecured(block: Route.() -> Unit){
    val wraptor =  application.getRestWrapTor().getOrConfigurationEx(
        "Wraptor not found in Application registry",
        ExceptionCodes.KEY_REGISTRATION)

    val serviceName = wraptor.authConfig.jwtServiceName
    authenticate(serviceName) {
        block.invoke(this)
    }
}
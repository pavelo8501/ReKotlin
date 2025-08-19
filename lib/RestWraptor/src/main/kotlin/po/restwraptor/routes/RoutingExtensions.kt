package po.restwraptor.routes

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import po.misc.types.getOrThrow
import po.restwraptor.RestWrapTor
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.exceptions.configException
import po.restwraptor.extensions.getRestWrapTor
import po.restwraptor.extensions.getWrapTorForced
import po.restwraptor.plugins.CoreAuthRoutePlugin

internal fun partsToUrl(pathParts: List<String>, routeGroupPath : String? = null):  String {

    val listToModify = pathParts.toMutableList()
    if(routeGroupPath  != null){
        listToModify.add(0, routeGroupPath)
    }
    val result =  listToModify
        .asSequence()
        .map { it.trim().trim('/') }
        .filter { it.isNotEmpty() }
        .joinToString(separator =  "/")

    return  result
}


fun Routing.getWrapTorForced(): RestWrapTor = application.getWrapTorForced()

fun Route.rootPath():String = application.rootPath

fun Routing.withBaseUrl(vararg pathParts:String): String = partsToUrl(pathParts.toList())
fun Route.withBaseUrl(vararg pathParts:String): String = partsToUrl(pathParts.toList())


fun Routing.jwtSecured(block: Route.() -> Unit){
    val wraptor =  application.getRestWrapTor().getOrThrow(this) {_->
        configException("Wraptor not found in Application registry", ExceptionCodes.KEY_REGISTRATION)
    }
    val serviceName : String =  wraptor.wrapConfig.authConfig.jwtServiceName

    authenticate(serviceName){
        install(CoreAuthRoutePlugin){

        }

        block()
    }

}
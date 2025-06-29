package po.restwraptor.extensions

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import po.misc.types.getOrManaged
import po.misc.types.getOrThrow
import po.restwraptor.RestWrapTor
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.exceptions.configException
import po.restwraptor.plugins.CoreAuthRoutePlugin

private fun partsToUrl(pathParts: List<String>):  String {
    val result =  pathParts
        .asSequence()
        .map { it.trim().trim('/') }
        .filter { it.isNotEmpty() }
        .joinToString("/")
    return  result
}

fun Routing.getWrapTor(): RestWrapTor{
   val wrapTor = application.getRestWrapTor().getOrManaged("Routing.getWrapTor")
   return wrapTor
}


fun Routing.baseApi():String{
    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig().baseApiRoute
    }
    return ""
}

fun Route.toUrl(vararg pathParts:String ):  String = partsToUrl(pathParts.toList())
fun Route.withBaseUrl(vararg pathParts:String ): String{

    val wraptor =  application.getRestWrapTor().getOrThrow<RestWrapTor, ConfigurationException>(null){msg->
        configException("Wraptor not found in Application registry",  ExceptionCodes.KEY_REGISTRATION)
    }
    var baseRouteToUse = wraptor.wrapConfig.apiConfig.baseApiRoute
    if(baseRouteToUse.isEmpty()){ baseRouteToUse =  application.rootPath }

    val list = mutableListOf<String>(baseRouteToUse)
    list.addAll(pathParts.toList())
    return partsToUrl((list))
}

fun Routing.jwtSecured(block: Route.() -> Unit){
    val wraptor =  application.getRestWrapTor().getOrThrow<RestWrapTor, ConfigurationException>(null) {_->
        configException("Wraptor not found in Application registry", ExceptionCodes.KEY_REGISTRATION)
    }
    val serviceName : String =  wraptor.wrapConfig.authConfig.jwtServiceName

    authenticate(serviceName){
        install(CoreAuthRoutePlugin){

        }

        block()
    }

}
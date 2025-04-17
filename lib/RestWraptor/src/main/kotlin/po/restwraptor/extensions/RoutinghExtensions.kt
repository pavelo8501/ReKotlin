package po.restwraptor.extensions

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application

fun partsToUrl(pathParts: List<String>):  String {
   val result =  pathParts
        .asSequence()
        .map { it.trim().trim('/') }
        .filter { it.isNotEmpty() }
        .joinToString("/")
    return  result
}


fun Application.toUrl(vararg pathParts:String ) = partsToUrl(pathParts.toList())

fun Application.withBaseUrl(vararg pathParts:String ): String{
    val list = mutableListOf<String>(rootPath)
    list.addAll(pathParts)
    return partsToUrl((list))
}

fun Route.toUrl(vararg pathParts:String ):  String = partsToUrl(pathParts.toList())
fun Route.withBaseUrl(vararg pathParts:String ): String{

    val list = mutableListOf<String>(application.rootPath)
    list.addAll(pathParts)
    return partsToUrl((list))

}

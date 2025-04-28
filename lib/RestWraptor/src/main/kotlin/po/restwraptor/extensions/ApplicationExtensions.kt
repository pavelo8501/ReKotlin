package po.restwraptor.extensions

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import po.misc.exceptions.getOrThrow
import po.restwraptor.RestWrapTor
import po.restwraptor.RestWrapTorKey
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.models.server.WraptorRoute
import po.restwraptor.scope.ConfigContext

private fun partsToUrl(pathParts: List<String>):  String {
    val result =  pathParts
        .asSequence()
        .map { it.trim().trim('/') }
        .filter { it.isNotEmpty() }
        .joinToString("/")
    return  result
}


/**
 * Retrieves the `RestWrapTor` instance from the application's attributes.
 *
 * This function allows access to the globally registered `RestWrapTor` instance
 * within the Ktor `Application` context.
 *
 * @return The `RestWrapTor` instance if it is registered, otherwise `null`.
 */
fun Application.getRestWrapTor(): RestWrapTor? {
    return attributes.getOrNull(RestWrapTorKey)
}



fun Application.getWraptorRoutes(callback: (List<WraptorRoute>)-> Unit ){
   val wraptor =  getRestWrapTor().getOrThrow<RestWrapTor, ConfigurationException>(
       "Wraptor not found in Application registry",
       ExceptionCodes.KEY_REGISTRATION.value)

   wraptor.getRoutes(callback)
}


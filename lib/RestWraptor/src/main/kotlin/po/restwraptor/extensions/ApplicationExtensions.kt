package po.restwraptor.extensions

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import po.restwraptor.RestWrapTor
import po.restwraptor.RestWrapTorKey
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

/**
 * Applies configuration to the `RestWrapTor` instance dynamically.
 *
 * This function provides a **clean and fluent API** for modifying `RestWrapTor` settings
 * **after it has been created**. It is useful for applying configurations such as:
 * - Enabling/disabling CORS
 * - Modifying authentication settings
 * - Custom API behavior
 *
 * @param configFn The function that applies additional configuration settings to `RestWrapTor`.
 *
 * **Usage Example:**
 * ```kotlin
 * application {
 *     install(Routing)
 *     val apiServer = RestWrapTor()
 *     apiServer.usePreconfiguredApp(this)
 *
 *     // Dynamically configure RestWrapTor using Application context
 *     this@application.configServer {
 *         enableCORS()
 *         enableAuthentication()
 *     }
 * }
 * ```
 */
//fun Application.configServer(configFn: suspend ConfigContext.()-> Unit){
//   attributes.allKeys.firstOrNull { it.name == "RestWrapTorInstance" }.let {
//       getRestWrapTor()?.applyConfig(configFn)
//   }
//}


fun Application.getWraptorRoutes(): List<WraptorRoute>{
   val wraptor =  getRestWrapTor().getOrConfigurationEx(
       "Wraptor not found in Application registry",
       ExceptionCodes.KEY_REGISTRATION)

   return wraptor.getRoutes()
}


fun Application.toUrl(vararg pathParts:String ) = partsToUrl(pathParts.toList())

fun Application.withBaseUrl(vararg pathParts:String ): String{
    val list = mutableListOf<String>(rootPath)
    list.addAll(pathParts)
    return partsToUrl((list))
}

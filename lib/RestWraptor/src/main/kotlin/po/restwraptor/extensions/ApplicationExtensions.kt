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
import po.restwraptor.scope.ConfigContext
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
fun Application.configServer(configFn: ConfigContext.()-> Unit){
   attributes.allKeys.firstOrNull { it.name == "RestWrapTorInstance" }.let {
       getRestWrapTor()?.applyConfig(configFn)
   }
}


fun Routing.baseApi():String{
    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig()?.baseApiRoute
    }
    return ""
}


fun Routing.wraptorPost(path: String, context: suspend RoutingContext.()-> Unit ): Route? {
    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig()?.let { conf ->
           this.post(application.toUrl(conf.baseApiRoute, path)){
               this.context()
               return@post
           }
        }
    }
    return null
}

fun Routing.wraptorPut(path: String, context: suspend RoutingContext.() -> Unit): Route? {
    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig()?.let { conf ->
            this.put(application.toUrl(conf.baseApiRoute, path)) {
                this.context()
                return@put
            }
        }
    }
    return null
}


fun Routing.wraptorGet(path: String, secure: Boolean,  context: suspend RoutingContext.()-> Unit ): Route? {

    application.getRestWrapTor()?.let { wraptor ->
        wraptor.getConfig()?.let { conf ->
            this.get(application.toUrl(conf.baseApiRoute, path)){
                this.context().apply { secure }
                return@get
            }
        }
    }
    return null
}
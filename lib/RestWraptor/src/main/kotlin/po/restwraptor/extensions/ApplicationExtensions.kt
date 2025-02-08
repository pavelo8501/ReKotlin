package po.restwraptor.extensions

import io.ktor.server.application.Application
import po.restwraptor.RestWrapTor
import po.restwraptor.RestWrapTorKey
import po.restwraptor.classes.ConfigContext

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
# RestWraptor

RestWraptor is a lightweight Kotlin wrapper around Ktor designed to
simplify REST API creation, authentication, and database interaction. It
provides a declarative, structured approach to route management and
request handling while integrating seamlessly with Exposed, session
tracking, and custom logging.

## Features

-   🚀 Simplified REST API creation\
-   🔐 Built-in authentication flow with session management\
-   🧩 Declarative routing via `managedRoutes`\
-   📦 DTO-based querying and updates\
-   🗂 Automatic error handling for public routes\
-   📝 Unified logging extraction for every request\
-   🔄 Async and coroutine‑friendly design\
-   🔧 Configurable server setup (host, port, routes, keys)

------------------------------------------------------------------------

## Example: Authentication

``` kotlin
suspend fun authenticate(
    userLogin: String,
    session: AuthorizedSession = getOrNewSession(createDefaultIdentifier())
): AuthenticationPrincipal? {

    val query = UserDTO.queryBuilder {
        equals(Users.login, userLogin)
    }
    return with(session) { pick(UserDTO, query) }.data
}
```

------------------------------------------------------------------------

## Configure RestWraptor Server

``` kotlin
fun configServer() {
    val keyPath = setKeyBasePath("keys")
    configureWraptor {

        setupRoutes {
            publicRoutes()
            authenticationRoutes()
        }

        registerResponseProvider {
            ApiResponse("")
        }

        authConfig.defaultSecurityRouts = false
        setupAuthentication(
            keyPath.readCryptoRsaKeys("ktor.pk8", "ktor.spki"),
            ::authenticate
        )
    }

    configureApplication {
        rootPath = "/backend"
        routing {
            // additional custom routing
        }
    }
}
```

------------------------------------------------------------------------

## Run the Server

``` kotlin
fun runServer(
    hoconConfig : HoconApplicationConfig
){
    runWraptor(hoconConfig.host, hoconConfig.port, wait = true) { handler ->
        handler.wraptorRoutes.forEach { it.output() }
    }
}
```

------------------------------------------------------------------------

## Full Example: Startup Sequence

``` kotlin
fun main() {
    val hoconConfig = HoconApplicationConfig(ConfigFactory.load("application.conf"))

    val connectionInfo = ConnectionInfo(
        host = hoconConfig.tryGetString("ktor.database.DB_HOST") ?: "0.0.0.0",
        port = hoconConfig.tryGetString("ktor.database.DB_HOST") ?: "5432",
        dbName = hoconConfig.tryGetString("ktor.database.POSTGRES_DB") ?: "*******",
        user = hoconConfig.tryGetString("ktor.database.POSTGRES_USER") ?: "*****",
        pwd =  hoconConfig.tryGetString("ktor.database.POSTGRES_PASSWORD") ?: "********",
    )

    DatabaseManager.openConnection(
        connectionInfo,
        ConnectionSettings(retries = 5)
    ) {
        newConnection { connection ->
            connection.databaseConfig()
            configServer()
            runServer(hoconConfig)
        }
    }
}
```

------------------------------------------------------------------------

## Declarative Public Routes

``` kotlin
fun ManagedRouting.publicRoutes() {
    managedRoutes {

        managedGet("pages") { session ->
            runTaskAsync("managedGet(Pages)") {
                session.runWithSession {
                    val result = select(PageDTO)
                    val response = ApiResponse(result.dataUnsafe)
                    val jsString = extractLog<LogData>().toJson()
                    response.provideLogData {
                        jsonString(jsString)
                        sessionData(session)
                    }
                    call.respond<ApiResponse<List<Page>>>(response)
                }
            }.onFailureCause {
                call.publicRouteErrorHandling(it)
            }
        }

        // Additional page, section and content-block routes...

        managedWrongPath {
            call.handleNotFound()
        }
    }
}
```

------------------------------------------------------------------------

## Summary

RestWraptor provides:

-   Clean configuration\
-   Strong typed DTO-driven database flow\
-   Pluggable authentication\
-   Resilient routing abstractions\
-   Automatic logging and error tracing

Perfect for medium-to-large Kotlin backend projects that need clean
structure and maintainability.

------------------------------------------------------------------------

## License

MIT (or your preferred license)


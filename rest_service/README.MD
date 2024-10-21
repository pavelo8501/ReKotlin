# REST Service Wrapper Library

## Overview
The RestServer wrapper library simplifies the creation and configuration of Ktor RESTful services in Kotlin. 
It provides a streamlined way to set up a server with common features such as authentication, rate limiting, CORS, content negotiation, and logging, 
allowing developers to focus on implementing business logic.

## Features

- **JWT Authentication**: Easily enable JWT authentication with customizable configurations.
- **Rate Limiting**: Protect your API from abuse with built-in rate limiting.
- **CORS Support**: Configure Cross-Origin Resource Sharing settings effortlessly.
- **Content Negotiation**: Automatically handle content negotiation for JSON. Or serialization with customizable serializers.
- **Logging Service**: Integrated and configurable logging for tracking application events and errors.
- **Error Handling**: Centralized error handling for easy debugging and maintenance.
- **Request Validation**: Validate incoming requests with a simple DSL.
- **Default Endpoints**:  Includes default endpoints for status checks and authentication.
- **Customizable Configuration**: Highly customizable to suit various application needs. Easily configure the server with a simple DSL, or use the default settings.

## Getting Started

### Prerequisites

- Kotlin
- Gradle
- Ktor

### Installation

Add the following dependency to your project's `build.gradle.kts` file:

```kotlin

repositories {
    mavenCentral() // or another repository

    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}


dependencies {
    implementation("com.github.pavelo8501:rest-service:0.10.22")
}
```
#### Basic Setup

```kotlin
import po.api.rest_service.RestServer

fun main() {
    RestServer.create().start()
}
```
This will start a server listening on 0.0.0.0:8080 with default configurations.

#### Custom Configuration

```kotlin

import io.ktor.server.application.Application
import po.api.rest_service.RestServer

fun main() {
    RestServer.create { 
        // Custom application configuration
        // For example, install additional plugins or define routes
    }.configureHost("127.0.0.1", 8081)
     .start()
}
```
#### API Configuration

```kotlin
import po.api.rest_service.RestServer
import po.api.rest_service.server.ApiConfig

val customConfig = ApiConfig(
    enableRateLimiting = true,
    enableDefaultSecurity = true,
    enableDefaultCors = true,
    enableDefaultContentNegotiation = true
).apply {
    baseApiRoute = "/api/v1"
    setAuthKeys(publicKeyString, privateKeyString)
}

fun main() {
    RestServer.create {
        // Use custom configuration
        RestServer.apiConfig = customConfig
    }.start()
}
```
### Features in Detail

1. **JWT Authentication**
1.1 **RestServer supports JWT authentication out of the box. You can configure it with your own keys:**

```kotlin

import po.api.rest_service.RestServer

fun main() {
    RestServer.create {
        RestServer.apiConfig.setAuthKeys(
            publicKeyString = "your-public-key",
            privateKeyString = "your-private-key"
        )
    }.start()
}
```

1.2 **Implement the SecureUserContext interface to define the user context for JWT payloads**

```kotlin

import po.api.rest_service.common.SecureUserContext

data class UserContext(
    override val username: String,
    override val roles: List<String>
) : SecureUserContext {
    override fun toPayload(): String {
        // Convert user data to JSON or other format
        return "{ \"username\": \"$username\", \"roles\": ${roles.joinToString()} }"
    }
}
```

1.3 **Set up the login callback to authenticate users**
    
```kotlin

RestServer.create {
    onLoginRequest = { loginData ->
        // Authenticate the user (e.g., check username and password)
        if (loginData.value.username == "admin" && loginData.value.password == "password") {
            UserContext(username = "admin", roles = listOf("admin"))
        } else {
            null
        }
    }
}.start()
```
2. **Rate Limiting**

Enable rate limiting to prevent abuse:
    
```kotlin
import po.api.rest_service.RestServer
import po.api.rest_service.plugins.RateLimiterConfig

RestServer.create {
    RestServer.apiConfig.enableRateLimiting = true
    RestServer.apiConfig.rateLimiterConfig = RateLimiterConfig().apply {
        requestsPerMinute = 100
        suspendInSeconds = 60
    }
}.start()
```
3. **CORS Configuration**

Customize CORS settings
    
```kotlin
import io.ktor.http.HttpMethod
import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.cors.routing.CORS
import po.api.rest_service.RestServer

RestServer.create {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowHost("example.com")
    }
}.start()
```

4. **Content Negotiation**

Set up content negotiation for JSON serialization
        
```kotlin
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import po.api.rest_service.RestServer
import kotlinx.serialization.json.Json

RestServer.create {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}.start()
```

5. **Logging Service**

Use the integrated LoggingService for application logging:

```kotlin

import po.api.rest_service.RestServer

RestServer.create {
    apiLogger.info("Server is starting")
    // Additional configurations
}.start()
```
6. **Default Endpoints**

Status Endpoint

GET /api/status

curl http://localhost:8080/api/status

Example Response:

```json
{
    "status": "ok"
}
```

Authentication Endpoint

POST /api/login

example request payload:

```json
{
  "data": {
    "type": "credentials",
    "value": {
      "username": "admin",
      "password": "password"
    }
  }
}
```
Custom Routes

Add your own routes within the application configuration block:    

```kotlin
RestServer.create {
    routing {
        get("/api/hello") {
            call.respondText("Hello, World!")
        }
    }
}.start()
```

7. **Extending RestServer**

You can extend RestServer to add additional functionality:
    
```kotlin
class MyRestServer : RestServer() {
    override fun configure(application: Application): Application {
        super.configure(application)
        application.apply {
            // Additional configuration
        }
        return application
    }
}
```

8. **Error Handling**

Handle exceptions and errors using Ktor's mechanisms or customize as needed.


## Dependencies

- Ktor: The Kotlin web framework used for building asynchronous servers and clients.
- Kotlinx Serialization: A Kotlin serialization library for JSON and other formats.
- Kotlinx Coroutines: A library for asynchronous programming in Kotlin.
- JWT: A JSON Web Token library for Kotlin.

Ensure these dependencies are included in your project.

```kotlin

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.4")
    implementation("io.ktor:ktor-serialization:1.6.4")
    implementation("io.ktor:ktor-auth-jwt:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.auth0:java-jwt:3.18.1")
}
```

### Contributing
Contributions are welcome! Please submit issues and pull requests for any improvements or bug fixes.

### License
This project is licensed under the MIT License.

### Acknowledgments

Ktor Framework: https://ktor.io/
Kotlin Language: https://kotlinlang.org/
Kotlinx Serialization:


package po.api.rest_service

import io.ktor.client.request.accept
import io.ktor.http.*

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import kotlinx.serialization.modules.polymorphic
import po.api.rest_service.logger.LogLevel
import po.api.rest_service.models.DefaultLoginRequest
import po.api.rest_service.models.DeleteRequestData
import po.api.rest_service.models.LoginRequestData
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.SelectRequestData
import po.api.rest_service.models.UpdateRequestData

class ApiServerTest {

    @Test
    fun `test logger is registered in attributes`() = testApplication {
        application {
            RestServer().start()
            val logger = this.attributes[RestServer.loggerKey]
            assertNotNull(logger, "Logger should be registered in the application attributes.")
        }
    }

    @Test
    fun `test status route returns OK`() = testApplication {
        application {
            RestServer {
                // Setup application
            }.start()
        }

        // Make a GET request to the /api/status endpoint
        val response = client.get("/api/status")
        assertEquals(200, response.status.value)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `test ContentNegotiation plugin is installed`() = testApplication {
        application {
            RestServer {
                // Configuration block
            }.start()
        }

        // Make a request to a route that requires JSON serialization
        val response = client.get("/api/status") {
            accept(ContentType.Application.Json)
        }

        // Check if the response is in JSON format
        assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), response.contentType())
    }

    @Test
    fun `test CORS plugin is installed`() = testApplication {
        application {
            RestServer {
                // Configuration block
            }.start()
        }

        // Make a request to check CORS headers
        val response = client.options("/api/status") {
            header(HttpHeaders.Origin, "http://localhost")
            method = HttpMethod.Options
        }

        // Verify that the Access-Control-Allow-Origin header is present
        assertEquals("*", response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `test logging occurs when status endpoint is called`() = testApplication {


        var logMessage: String? = null

        application {
            RestServer {
                apiLogger.registerLogFunction(LogLevel.MESSAGE) { msg, _, _, _ ->
                    logMessage = msg
                }
            }.start()
        }

        val response = client.get("/api/status")
        assertEquals(HttpStatusCode.OK, response.status)

        assertEquals("Status endpoint called.", logMessage)
    }

    @Test
    fun `test configureHost sets host and port correctly`() {
        val apiServer = RestServer().configureHost("127.0.0.1", 8081)
        assertEquals("127.0.0.1", apiServer.host)
        assertEquals(8081, apiServer.port)
    }

    @Test
    fun `test jsonDefault with polymorphic configuration`() {

        val json = RestServer.jsonDefault {
            polymorphic(RequestData::class) {
                subclass(SelectRequestData::class, SelectRequestData.serializer())
                subclass(UpdateRequestData::class, UpdateRequestData.serializer())
                subclass(DeleteRequestData::class, DeleteRequestData.serializer())
                subclass(LoginRequestData::class, LoginRequestData.serializer())
            }
        }

        val data = LoginRequestData(DefaultLoginRequest("username", "password"))
        val serialized = json.encodeToString(RequestData.serializer(), data)
        val deserialized = json.decodeFromString(RequestData.serializer(), serialized)

        assertEquals(data, deserialized)
    }

}
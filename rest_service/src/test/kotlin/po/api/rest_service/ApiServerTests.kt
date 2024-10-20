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
import kotlin.text.get

class ApiServerTest {

    @Test
    fun `server starts with no params supplied`() = testApplication {
        application {
            RestServer().configure(this)
        }
        val response = client.get("/api/status")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }


    @Test
    fun `test logger is registered in attributes`() = testApplication {
        application {
            RestServer().configure(this)
            val logger = this.attributes[RestServer.loggerKey]
            assertNotNull(logger, "Logger should be registered in the application attributes.")
        }
    }

    @Test
    fun `test status route returns OK`() = testApplication {
        application {
            RestServer().configure(this)
        }

        // Make a GET request to the /api/status endpoint
        val response = client.get("/api/status")
        assertEquals(200, response.status.value)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `test ContentNegotiation plugin is installed`() = testApplication {
        application {
            RestServer().configure(this)
        }
        val response = client.get("/api/status-json") {
            accept(ContentType.Application.Json)
        }
        assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), response.contentType())
    }

    @Test
    fun `test CORS plugin is installed`() = testApplication {
        application {
            RestServer().configure(this)
        }
        val response = client.options("/api/status") {
            header(HttpHeaders.Origin, "http://localhost") // Ensure the correct origin is set
        }
        println("Response headers: ${response.headers}")
        println("Status: ${response.status}")
        val corsHeader = response.headers[HttpHeaders.AccessControlAllowOrigin]
        assertNotNull(corsHeader, "CORS header should be present")
        assertEquals("*", corsHeader)
    }

    @Test
    fun `test logging occurs when status endpoint is called`() = testApplication {
        var logMessage: String? = null

        application {
            RestServer {
                apiLogger.registerLogFunction(LogLevel.MESSAGE) { msg, _, _, _ ->
                    logMessage = msg
                }
            }.configure(this)
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
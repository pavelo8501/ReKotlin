package po.api.rest_service.plugins

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import po.api.rest_service.RestServer
import po.api.rest_service.jwtService
import po.api.rest_service.security.JWTService
import po.api.rest_service.security.JwtConfig
import kotlin.test.assertFalse
import kotlin.test.assertNotNull


class JwtPluginTest {


    @Test
    fun `Jwt plugin installs correctly`() = testApplication {
        application {
            RestServer().configure(this)
            assertNotNull(this.jwtService, "JWT plugin should be installed and jwtService should not be null.")
        }
    }

    @Test
    fun `Jwt plugin handles missing configuration`() = testApplication {
        application {
            RestServer().configure(this)
            assertFalse(this.jwtService.ready)
        }
    }

}
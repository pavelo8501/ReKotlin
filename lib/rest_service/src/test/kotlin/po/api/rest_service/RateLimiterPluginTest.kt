package po.api.rest_service

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import po.api.rest_service.plugins.RateLimiter
import kotlin.test.assertEquals

class RateLimiterPluginTest {

    @Test
    fun `test rate limiter enforced suspension`() = testApplication {
        application {
            install(RateLimiter) {
                requestsPerMinute = 5
            }
            routing {
                get("/test") {
                    call.respondText("OK")
                }
            }
        }
        repeat(5) {
            val response = client.get("/test")
            assertEquals(HttpStatusCode.OK, response.status)
        }
        val response = client.get("/test")
        assertEquals(HttpStatusCode.TooManyRequests, response.status)
    }

    @Test
    fun `test rate limiting after time suspension time expires`() = testApplication {
        application {
            install(RateLimiter) {
                requestsPerMinute = 2
                suspendInSeconds = 1
            }
            routing {
                get("/test") {
                    call.respondText("OK")
                }
            }
        }
        repeat(2) {
            val response = client.get("/test")
            assertEquals(HttpStatusCode.OK, response.status)
        }

        var response = client.get("/test")
        assertEquals(HttpStatusCode.TooManyRequests, response.status)

        Thread.sleep(1010)
        response = client.get("/test")
        assertEquals(HttpStatusCode.OK, response.status)
    }

}
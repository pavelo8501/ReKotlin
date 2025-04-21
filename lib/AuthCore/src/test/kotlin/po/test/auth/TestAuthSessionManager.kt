package po.test.auth

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import po.auth.AuthSessionManager
import po.auth.sessions.models.AuthorizedSession

class TestAuthSessionManager {

    @Test
    fun testService(){
        runBlocking {
            val session = AuthSessionManager.createAnonymousSession()
            withContext(session) {
               val retrieved =  coroutineContext[AuthorizedSession]
                println("Inside context -> ${retrieved?.sessionId}")
            }
        }
    }
}
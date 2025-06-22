package po.test.auth

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import po.auth.AuthSessionManager
import po.auth.extensions.currentSession
import po.auth.extensions.withSessionContext
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.misc.coroutines.getCoroutineInfo
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class  authData2(override val sessionID: String, override val remoteAddress: String) : SessionIdentified


class TestAuthSessionManager {

    @Test
    fun testSession()= runTest {

        withContext(AuthSessionManager.getOrCreateSession(authData2("id", "127.0.0.1")).sessionContext) {
            val retrieved = coroutineContext[AuthorizedSession]
            assertNotNull(retrieved)
            assertNotNull(retrieved.sessionID)
            println("Inside context -> ${retrieved.sessionID}")
        }
    }

    @Test
    fun `withExtension executes correctly` () = runTest {
        val session = AuthSessionManager.getOrCreateSession(authData2("id", "127.0.0.1"))
        withSessionContext(session){
            val retrieved = coroutineContext[AuthorizedSession]
            val coroutineInfo = getCoroutineInfo()

            assertNotNull(retrieved, "in testSessionWithExtension")
            assertNotNull(retrieved.sessionID)
            assertEquals("AnonymousSession", coroutineInfo.name)
        }
    }

    @Test
    fun `current session can be retrieved from the contex` () = runTest {

        val session = AuthSessionManager.getOrCreateSession(authData2("id", "127.0.0.1"))
        withSessionContext(session){
            val retrieved = currentSession()
            assertNotNull(retrieved, "in testSessionWithExtension")
            assertNotNull(retrieved.sessionID)
            assertEquals("AnonymousSession", retrieved.coroutineName)
        }
    }
}
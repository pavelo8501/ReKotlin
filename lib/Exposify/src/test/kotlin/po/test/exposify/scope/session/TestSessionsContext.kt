package po.test.exposify.scope.session

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import po.auth.sessions.interfaces.SessionIdentified
import po.test.exposify.setup.DatabaseTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionsContext : DatabaseTest()  {
    class SessionIdentity(override val sessionID: String, override val remoteAddress: String): SessionIdentified

    companion object{
        @JvmStatic()
        var userId : Long = 1
    }


    fun setup() = runTest {
//
//        val user = User(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null"
//        )
//        withConnection {
//
//            service(UserDTO.Companion, TableCreateMode.Create) {
//                userId = update(user).getDataForced().id
//            }
//
//            service(PageDTO.Companion, TableCreateMode.Create) {
//                sequence(PageDTO.Companion.UPDATE) { handler ->
//                    update(handler.inputList)
//                }
//                sequence(PageDTO.Companion.SELECT) { handler ->
//                    select()
//                }
//            }
//        }
    }



    fun `Test anonymous session flow`() = runTest {
//
//        val pages = pageModels(pageCount = 4, updatedBy = userId)
//        val session = session(SessionIdentity("0", "192.169.1.1"))
//
//        var sessionIdOnStart: String? = null
//        var sessionIdOnComplete: String? = null
//
//        var coroutineInfo: CoroutineInfo? = null
//        var sessionAfter: AuthorizedSession? = null
//         lateinit var result: ResultList<PageDTO, Page, PageEntity>
//
//        withSessionContext(session) {
//            result = runSequence(PageDTO.Companion.UPDATE) {
//                onStart {
//                    sessionIdOnStart = it.sessionID
//                    coroutineInfo = it.coroutineInfo
//                }
//                onComplete {
//                    sessionIdOnComplete = it.sessionID
//                    sessionAfter = it.session
//                }
//                withData(pages)
//            }
//        }
//        assertNotNull(coroutineInfo, "Coroutine info not available on sequence start")
//        assertEquals(pages.size, result.size, "Input and output count mismatch")
//        assertNotNull(sessionAfter, "Session can not be retrieved after process complete")
//        assertNotNull(sessionIdOnComplete, "onComplete never hit")
//        assertEquals(session.sessionID, sessionIdOnStart, "Sequence started with different SessionId: ${session.sessionID}")
//        assertEquals(sessionIdOnStart, sessionIdOnComplete, "Sequence completed with different SessionId: ${sessionIdOnComplete}")
//        assertEquals(sessionIdOnComplete, sessionAfter.sessionID, "Process exits with different SessionId ${sessionAfter.sessionID}")
    }


    fun `Authenticated session`() = runTest {
//
//        val user = User(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null"
//        )
//        user.id = userId
//        fun userLookUp(login: String): AuthenticationPrincipal? {
//            return user
//        }
//
//        val session = session(SessionIdentity("1", "192.169.1.2"))
//        var sessionType: SessionType? = null
//
//        withSessionContext(session) {
//            registerAuthenticator(::userLookUp)
//            val principal = session.authenticate("some_login", "password")
//            runSequence(PageDTO.Companion.SELECT) {
//                onStart {
//                    sessionType = it.session.sessionType
//                }
//            }
//            assertNotNull(principal, "Authenticate failed")
//            assertEquals("some_login", principal.login)
//            val onStartSessionType = assertNotNull(sessionType, "onStart never hit")
//            assertEquals(SessionType.USER_AUTHENTICATED, onStartSessionType, "Session type mismatch")
//        }
    }
}
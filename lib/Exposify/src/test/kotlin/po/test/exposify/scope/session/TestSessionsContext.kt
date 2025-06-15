package po.test.exposify.scope.session

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.authenticate
import po.auth.extensions.generatePassword
import po.auth.extensions.registerAuthenticator
import po.auth.extensions.session
import po.auth.extensions.withSessionContext
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultList
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.enums.TableCreateMode
import po.misc.coroutines.CoroutineInfo
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionsContext : DatabaseTest()  {
    class SessionIdentity(override val sessionID: String, override val remoteAddress: String): SessionIdentified

    companion object{
        @JvmStatic()
        var userId : Long = 1
    }

    @BeforeAll
    fun setup() = runTest {

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection {

            service(UserDTO.Companion, TableCreateMode.CREATE) {
                userId = update(user).getDataForced().id
            }

            service(PageDTO.Companion, TableCreateMode.CREATE) {
                sequence(PageDTO.Companion.UPDATE) { handler ->
                    update(handler.inputList)
                }
                sequence(PageDTO.Companion.SELECT) { handler ->
                    select()
                }
            }
        }
    }


    @Test
    fun `Test anonymous session flow`() = runTest {

        val pages = pageModels(pageCount = 4, updatedBy = userId)
        val session = session(SessionIdentity("0", "192.169.1.1"))

        var sessionIdOnStart: String? = null
        var sessionIdOnComplete: String? = null

        var coroutineInfo: CoroutineInfo? = null
        var sessionAfter: AuthorizedSession? = null
         lateinit var result: ResultList<PageDTO, Page, PageEntity>

        withSessionContext(session) {
            result = runSequence(PageDTO.Companion.UPDATE) {
                onStart {
                    sessionIdOnStart = it.sessionID
                    coroutineInfo = it.coroutineInfo
                }
                onComplete {
                    sessionIdOnComplete = it.sessionID
                    sessionAfter = it.session
                }
                withData(pages)
            }
        }
        assertNotNull(coroutineInfo, "Coroutine info not available on sequence start")
        assertEquals(pages.size, result.size, "Input and output count mismatch")
        assertNotNull(sessionAfter, "Session can not be retrieved after process complete")
        assertNotNull(sessionIdOnComplete, "onComplete never hit")
        assertEquals(session.sessionID, sessionIdOnStart, "Sequence started with different SessionId: ${session.sessionID}")
        assertEquals(sessionIdOnStart, sessionIdOnComplete, "Sequence completed with different SessionId: ${sessionIdOnComplete}")
        assertEquals(sessionIdOnComplete, sessionAfter.sessionID, "Process exits with different SessionId ${sessionAfter.sessionID}")
    }

    @Test
    fun `Authenticated session`() = runTest {

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        user.id = userId
        fun userLookUp(login: String): AuthenticationPrincipal? {
            return user
        }

        val session = session(SessionIdentity("1", "192.169.1.2"))
        var sessionType: SessionType? = null

        withSessionContext(session) {
            registerAuthenticator(::userLookUp)
            val principal = session.authenticate("some_login", "password")
            runSequence(PageDTO.Companion.SELECT) {
                onStart {
                    sessionType = it.session.sessionType
                }
            }
            assertNotNull(principal, "Authenticate failed")
            assertEquals("some_login", principal.login)
            val onStartSessionType = assertNotNull(sessionType, "onStart never hit")
            assertEquals(SessionType.USER_AUTHENTICATED, onStartSessionType, "Session type mismatch")
        }
    }
}
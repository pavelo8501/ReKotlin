package po.test.exposify.scope

import kotlinx.coroutines.test.runTest
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.session
import po.auth.sessions.interfaces.SessionIdentified
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.pageModels
import kotlin.test.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.authenticate
import po.auth.extensions.generatePassword
import po.auth.extensions.registerAuthenticator
import po.auth.extensions.withSessionContext
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.models.AuthorizedSession
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.misc.exceptions.CoroutineInfo
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
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
    fun setup() = runTest{

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")
        startTestConnection{

            service(UserDTO, TableCreateMode.CREATE) {
                userId =  update(user).getDataForced().id
            }

            service(PageDTO, TableCreateMode.CREATE) {
                sequence(PageDTO.UPDATE) { handler->
                    update(handler.inputList)
                }
                sequence(PageDTO.SELECT) { handler ->
                    select()
                }
            }
        }
    }

    @DisplayName("Test anonymous session flow")
    @Test
    fun `Anonymous sessions`() = runTest{

        val pages = pageModels(pageCount = 4, updatedBy = userId)

        val session = session(SessionIdentity("0", "192.169.1.1"))

        var sessionIdOnStart : String? = null
        var sessionIdOnComplete : String? = null

        var coroutineInfo : CoroutineInfo? = null
        var sessionAfter : AuthorizedSession? = null
        var result : List<Page> = emptyList()

        withSessionContext(session) {
            result = runSequence(PageDTO.UPDATE) {
                onStart {


                    sessionIdOnStart = it.sessionID
                    coroutineInfo = it.coroutineInfo
                }
                onComplete {

                    sessionIdOnComplete = it.sessionID
                    sessionAfter = it.session
                }
                withData(pages)
            }.getData()
        }

       // assertNotNull(sessionOnStart, "Session not in the registry")
        assertNotNull(coroutineInfo, "Coroutine info not available on sequence start")
        assertEquals(pages.count(), result.count(), "Input and output count mismatch")
        assertNotNull(sessionAfter, "Session can not be retrieved after process complete")
        assertNotNull(sessionIdOnComplete, "onComplete never hit")
        assertAll(
            {assertEquals(
                session.sessionID,
                sessionIdOnStart,
                "Sequence started with different SessionId: ${session.sessionID}")},
            {assertEquals(sessionIdOnStart, sessionIdOnComplete,
                "Sequence completed with different SessionId: ${sessionIdOnComplete}")},
            {assertEquals(sessionIdOnComplete,
                sessionAfter?.sessionID,
                "Process exits with different SessionId ${sessionAfter?.sessionID}")}
        )
    }

    @DisplayName("Test authenticated session flow")
    @Test
    fun `Authenticated session`()= runTest{

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        user.id = userId
        fun userLookUp(login: String): AuthenticationPrincipal?{
            return user
        }
        val session = session(SessionIdentity("1", "192.169.1.2"))
        var sessionType : SessionType? = null

        withSessionContext(session) {
            registerAuthenticator(::userLookUp)
            val principal = session.authenticate("some_login", "password")
            runSequence(PageDTO.SELECT){
                onStart {
                    sessionType = it.session?.sessionType
                }
            }
            assertNotNull(principal, "Authenticate failed")
            assertEquals("some_login", principal.login)
            val onStartSessionType = assertNotNull(sessionType, "onStart never hit")
            assertEquals(SessionType.USER_AUTHENTICATED, onStartSessionType, "Session type mismatch")
        }
    }
}
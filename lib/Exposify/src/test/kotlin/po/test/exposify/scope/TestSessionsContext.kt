package po.test.exposify.scope

import kotlinx.coroutines.test.runTest
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.session
import po.auth.extensions.withSession
import po.auth.sessions.interfaces.SessionIdentified
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.service.enums.TableCreateMode
import po.misc.exceptions.getCoroutineInfo
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.pageModels
import kotlin.test.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.authenticate
import po.auth.extensions.currentSession
import po.auth.extensions.generatePassword
import po.auth.extensions.registerAuthenticator
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.models.AuthorizedSession
import po.misc.collections.generateKey
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

        user.hashedPassword = generatePassword("password")
        startTestConnection().let { connection ->

            connection.service(UserDTO, TableCreateMode.CREATE) {
                userId =  update(user).getDataForced().id
            }

            connection.service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                truncate()
                sequence(SequenceID.UPDATE) { handler->
                    update(handler.inputData)
                }

                sequence(SequenceID.SELECT) { handler ->
                   select()
                }
            }
        }
    }

//    @DisplayName("Test anonymous session flow")
//    @Test
//    fun `test anonymous sessions`() = runTest{
//
//        val user = User(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null")
//
//        user.id = userId
//        val pages = pageModels(pageCount = 4, updatedBy = userId)
//
//        val session = session(SessionIdentity("0", "192.169.1.1"))
//        withSession(session){
//            val sessionOnStart = coroutineContext[AuthorizedSession]
//
//            var sessionIdBeforeStart = ""
//            var sessionIdOnComplete = ""
//            val result = PageDTO.runSequence(SequenceID.UPDATE){
//                withInputData(pages)
//                onStart {
//                    sessionIdBeforeStart = it.sessionID
//                }
//
//                onComplete {
//                    sessionIdOnComplete = it.sessionID
//                }
//            }.getData()
//            val coroutineInfo = getCoroutineInfo()
//            val sessionAfter =  currentSession()
//
//            assertNotNull(sessionOnStart, "Session not in the registry")
//            assertNotNull(sessionAfter, "Session can not be retrieved after process complete")
//            assertEquals(session.coroutineName, coroutineInfo.name, "Session's coroutine name do not match current")
//            assertEquals(pages.count(), result.count(), "Input and output count mismatch")
//            assertAll(
//                {assertEquals(
//                    session.sessionID,
//                    sessionIdBeforeStart,
//                    "Sequence started with different SessionId: ${sessionIdBeforeStart}")},
//                {assertEquals(sessionIdBeforeStart,
//                    sessionIdOnComplete,
//                    "Sequence completed with different SessionId: ${sessionIdOnComplete}")},
//                {assertEquals(sessionIdOnComplete,
//                    sessionAfter.sessionID,
//                    "Process exits with different SessionId ${sessionAfter.sessionID}")}
//            )
//        }
//    }

//    @DisplayName("Test authenticated session flow")
//    @Test
//    fun `test authenticated session`()= runTest{
//
//        val user = User(
//            id = 0,
//            login = "some_login",
//            hashedPassword = generatePassword("password"),
//            name = "name",
//            email = "nomail@void.null")
//
//        user.id = userId
//        fun userLookUp(login: String): AuthenticationPrincipal?{
//            return user
//        }
//        val session = session(SessionIdentity("1", "192.169.1.2"))
//        var sessionType : SessionType = SessionType.ANONYMOUS
//        withSession(session) {
//
//            registerAuthenticator(::userLookUp)
//            val principal = session.authenticate("some_login", "password")
//            val pages = PageDTO.runSequence(SequenceID.SELECT){
//                onStart {
//                    sessionType = it.sessionType
//                }
//            }
//            assertNotNull(principal, "Authenticate failed")
//            assertEquals("some_login", principal.login)
//            assertEquals(sessionType, SessionType.USER_AUTHENTICATED)
//        }
//    }
}
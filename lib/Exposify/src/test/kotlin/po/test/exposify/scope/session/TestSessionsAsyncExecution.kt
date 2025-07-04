package po.test.exposify.scope.session

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.generatePassword
import po.auth.extensions.withSessionContext
import po.auth.extensions.withSessionSuspended
import po.auth.sessions.enumerators.SessionType
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.NotifyConfig
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionsAsyncExecution : DatabaseTest(), TasksManaged {

    companion object {

        @JvmStatic
        lateinit var authenticatedUser: User

        @JvmStatic
        fun validateUser(login: String): AuthenticationPrincipal? {
            return if (login == "some_login") {
                authenticatedUser
            } else {
                null
            }
        }

        @JvmStatic
        val session = TestSessionsContext.SessionIdentity("0", "192.169.1.1")

    }


    fun `authenticated and anonymous sessions execute sequences concurrently without interference`() = runTest {

        val authData = AuthenticationData(
            sessionID = "none",
            remoteAddress = "192.168.1.1",
            headers = emptyMap<String, String>(),
            requestUri = "",
            authHeaderValue = ""
        )

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        val anonSession = AuthSessionManager.getOrCreateSession(authData)
        val authSession = AuthSessionManager.getOrCreateSession(authData)

        withConnection {
            service(UserDTO.Companion, TableCreateMode.ForceRecreate) {
                authenticatedUser = update(user).getDataForced()
                AuthSessionManager.authenticator.setAuthenticator(::validateUser)
            }

            service(PageDTO.Companion, TableCreateMode.Create) {

                sequence(PageDTO.Companion.UPDATE) { handler ->
                    update(handler.inputList)
                }
                sequence(PageDTO.Companion.SELECT) { handler ->
                    select()
                }
            }
        }

        AuthSessionManager.authenticator.authenticate("some_login", "password", authSession)

        assertAll(
            "Assert session types correct. And data initialized",
            { assertEquals(SessionType.ANONYMOUS, anonSession.sessionType, "Session failed to authenticate") },
            { assertEquals(SessionType.USER_AUTHENTICATED, authSession.sessionType, "Session failed to authenticate") },
            { assertNotNull(authSession.principal, "AuthPrincipal uninitialized") },
            { assertEquals(user.id, authSession.principal?.id, "Id mismatch") },
            { assertEquals(user.login, authSession.principal?.login, "Login mismatch") }
        )

        logNotify().notifierConfig {
            console = NotifyConfig.ConsoleBehaviour.Mute
        }
        runBlocking {
            launch {
                withSessionSuspended(authSession) {
                    val inputData =
                        pageModelsWithSections(pageCount = 1000, sectionsCount = 10, authSession.principal!!.id)

                    runSequence(PageDTO.Companion.UPDATE) {
                        withData(inputData)
                    }
                }
            }
            launch {
                withSessionContext(anonSession) {
                    delay(200)
                    runSequence(PageDTO.Companion.SELECT) {

                    }
                }
            }
        }
    }
}
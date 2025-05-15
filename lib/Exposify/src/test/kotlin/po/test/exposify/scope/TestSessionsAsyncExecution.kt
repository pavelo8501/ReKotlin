package po.test.exposify.scope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.AuthSessionManager
import po.auth.authentication.authenticator.models.AuthenticationData
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.extensions.generatePassword
import po.auth.sessions.enumerators.SessionType
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.extensions.launchProcess
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
        lateinit var authenticatedUser: User

        @JvmStatic
        fun validateUser(login: String): AuthenticationPrincipal? {
            return if (login == "some_login") {
                authenticatedUser
            } else {
                null
            }
        }
    }

    @DisplayName("Test anonymous session flow")
    @Test
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

        anonSession.getLoggerProcess?.invoke()?.run {
            notifier.setNotifierConfig(NotifyConfig(console = ConsoleBehaviour.MuteInfo))
        }
        authSession.getLoggerProcess?.invoke()?.run {
            notifier.setNotifierConfig(NotifyConfig(console = ConsoleBehaviour.MuteInfo))
        }


        startTestConnection().run {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                authenticatedUser = update(user).getDataForced()
                AuthSessionManager.authenticator.setAuthenticator(::validateUser)
            }

            service(PageDTO, TableCreateMode.CREATE) {

                sequence(PageDTO.UPDATE) { handler->
                 update(handler.inputList)
                }
                sequence(PageDTO.SELECT) {handler->
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

        TasksManaged.notifier.setNotifierConfig(NotifyConfig(console = ConsoleBehaviour.Mute))


        runBlocking {
            launch {
                authSession.launchProcess {
                    val inputData =
                        pageModelsWithSections(pageCount = 1000, sectionsCount = 10, authSession.principal!!.id)

                    runSequence(PageDTO.UPDATE){
                        withData(inputData)
                    }

                }
            }
            launch {
                anonSession.launchProcess {
                    delay(200)
                    runSequence(PageDTO.SELECT){

                    }

                   // val selectionResult = PageDTO.runSequence(SequenceID.SELECT) {
                   //     onStart {
                    //        println("Running update with session ${it.sessionID}")
                   //     }
                  //  }
                }
            }
        }
    }
}
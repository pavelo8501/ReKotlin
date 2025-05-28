package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.extensions.createDefaultIdentifier
import po.auth.extensions.generatePassword
import po.auth.extensions.withSessionContext
import po.exposify.DatabaseManager
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.result.toResultList
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.scope.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Users
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCRUD : DatabaseTest(),  TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0

        @JvmStatic
        val session = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
    }




    @Test
    fun `Pick statement is processed in Sequence context`() = runTest {

        val loggerHandler: LogNotifyHandler = logNotify()
        loggerHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
        }

        val inputUser = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        startTestConnection{
            service(UserDTO){
                update(inputUser)
                sequence(UserDTO.PICK) { handler ->
                    pick(handler.query).toResultList()
                }
            }
        }

        var userFail: User? = null
        var userSuccess: User? = null
        withSessionContext(createDefaultIdentifier()) {
            userFail = runSequence(UserDTO.PICK) {
                withQuery {
                    WhereQuery(Users).equalsTo({ login }, "wrong")
                }
            }.getData().firstOrNull()
            userSuccess = runSequence(UserDTO.PICK) {
                withQuery {
                    WhereQuery(Users).equalsTo({ login }, inputUser.login)
                }
            }.getData().firstOrNull()
        }

        assertNull(userFail)
        val selectedUser = assertNotNull(userSuccess)
        assertEquals(selectedUser.name, inputUser.name)
        assertEquals(selectedUser.login, inputUser.login)
    }
    fun `test run n a real db with sequence select`() = runTest{

        val connectionInfo = ConnectionInfo(host ="0.0.0.0", port ="5432", dbName = "medprof_postgres", user = "django-api", pwd = "django-api_usrPWD12")
        DatabaseManager.openConnection(connectionInfo).service(PageDTO){
            sequence(PageDTO.SELECT) {
                select()
            }
        }
       val result = runSequence(PageDTO.SELECT).getData()

        assertTrue(result.isNotEmpty())
    }
}
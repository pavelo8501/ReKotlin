package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.extensions.generatePassword
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.toResultList
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.scope.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Users
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCRUD : DatabaseTest(), TasksManaged {

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

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                TestSequenceContext.updatedById = update(user).getDataForced().id
                sequence(UserDTO.PICK){handler->
                    pick(handler.query).toResultList()
                }
            }
            service(PageDTO){
                for(i in 1..3) {
                    val page = Page()
                    page.updatedById = user.id
                    page.sections.addAll(sectionsPreSaved(0L))
                    update(page)
                }
                select()
            }
        }
       val selected = runSequence(UserDTO.PICK){
            withQuery{
                WhereQuery(Users).equalsTo({login}, "some_login")
            }
        }.getData().firstOrNull()

        val selectedUser = assertNotNull(selected)
        assertEquals(selectedUser.name, user.name)
        assertEquals(selectedUser.login, user.login)

    }

}
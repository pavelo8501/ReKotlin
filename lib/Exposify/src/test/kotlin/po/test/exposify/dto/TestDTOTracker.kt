package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDTOTracker: DatabaseTest(), TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }

    @BeforeAll
    fun setup() = runTest {

        logNotify().notifierConfig { console = ConsoleBehaviour.MuteNoEvents }

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection{
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `Information updated and stored`() = runTest{

        var pageDTO : PageDTO? = null
        val page = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 1,
            contentBlocksCount = 1,
            updatedBy = updatedById).first()

        startTestConnection{
            service(PageDTO) {
                pageDTO = update(page).getDTO()
            }
        }
        val tracker = assertNotNull(pageDTO?.tracker, "Tracker is missing on DTO")
        assertTrue(tracker.records.isNotEmpty() ,"No records present")

    }

}
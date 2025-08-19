package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import po.auth.extensions.generatePassword
import po.exposify.common.events.DTOData
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.NotifyConfig
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDTOTracker: DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<out CTX> =  asIdentity()
    override val contextName: String = "TestDTOTracker"

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }

    @BeforeAll
    fun setup() = runTest {

        logHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
            allowDebug(DTOData)
        }

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        withConnection{
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(user).dataUnsafe.id
            }
        }
    }

    @Test
    fun `Information updated and stored`() = runTest{

        var updateResult : ResultSingle<PageDTO, Page>? = null
        val page = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 1,
            contentBlocksCount = 2,
            updatedBy = updatedById).first()

        withConnection{
            service(PageDTO) {
                updateResult = update(page)
            }
        }
        val pageDTOResult = assertNotNull(updateResult)

        val tracker = assertNotNull(pageDTOResult.getTracker(), "Tracker is missing on DTO")
        val hierarchy =  tracker.resolveHierarchy()
        assertTrue(tracker.trackRecords.isNotEmpty() ,"No records present")
        tracker.printTrace()
    }

}
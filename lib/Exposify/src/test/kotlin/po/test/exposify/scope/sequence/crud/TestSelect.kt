package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.query.deferredQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.scope.sequence.builder.select
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.launcher.launch
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPage
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSelect: DatabaseTest(), TasksManaged  {

    override val identity: CTXIdentity<TestSelect> = asIdentity()

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }
    @BeforeAll
    fun setup(){
        logHandler.notifierConfig {
            setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
        }
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }

        withConnection {
            val pages: List<Page> = mockPages(quantity = 2){index-> mockPage("Page_$index", updatedById) }
            service(PageDTO) {
                insert(pages)
                sequenced(PageDTO.Select) {handler ->
                    select(handler){

                    }
                }
            }
        }
    }

    @Test
    fun `Sequenced SELECT statement`(): TestResult = runTest{

        val result = with(mockedSession){ launch(PageDTO.Select) }
        assertIs<ResultList<*, *, *>>(result)
        assertTrue(!result.isFaulty)
        assertEquals(2, result.dto.size)
    }

    @Test
    fun `Sequenced SELECT statement with query`(): TestResult = runTest{

        val queriedPageName = "Page_2"
        val result = with(mockedSession){
            launch(PageDTO.Select, deferredQuery(PageDTO) { equals(Pages.name, queriedPageName) } )
        }
        assertEquals(1, result.dto.size)
        val persistedPage = assertNotNull(result.data.firstOrNull())
        assertEquals(queriedPageName, persistedPage.name)
    }

}
package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.update
import po.exposify.scope.sequence.launcher.launch
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPage
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUpdate: DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestUpdate> = asIdentity()

    companion object{
        @JvmStatic
        var updatedById : Long = 0

        @JvmStatic
        lateinit var page: Page
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

        page = mockPage("Sequenced_UPDATE_mock", updatedById)
        withConnection {
            service(PageDTO) {
                insert(page)
                sequenced(PageDTO.Update) { handler ->
                    update(handler){

                    }
                }
            }
        }
    }

    @Test
    fun `Sequenced UPDATE statement`(): TestResult = runTest{

        val updateValue = "DifferentName"

        val result = with(mockedSession){
            val updatedPage = page.copy()
            updatedPage.name = updateValue
            launch(PageDTO.Update, updatedPage)
        }

        assertIs<ResultSingle<*, *>>(result)
        val updatedData = assertNotNull(result.data, "Result failure")
        assertEquals(updateValue, updatedData.name, "Page data was not updated")
        val pageDTO = assertNotNull(result.dto, "Result failure")
        assertEquals(updateValue, pageDTO.name)
    }

}
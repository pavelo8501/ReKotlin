package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.pickById
import po.exposify.scope.sequence.builder.sequenced
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
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPick: DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestPick> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0
    }

    @BeforeAll
    fun setup() {
        logHandler.notifierConfig {
            setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
        }
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }

        withConnection {
            val pages: List<Page> = mockPages(quantity =  10){index->
                mockPage(name =  "page_$index", updatedById)
            }
            service(PageDTO) {
                insert(pages)
                sequenced(PageDTO.PICK) {handler->
                    pickById(handler){

                    }
                }
            }
        }
    }

    @Test
    fun `Sequenced PICK_BY_ID statement`(): TestResult = runTest{

        val idToPick = 1L
        val result =  with(mockedSession){
            launch(PageDTO.PICK, idToPick)
        }
        assertIs<ResultSingle<*, *, *>>(result)
        val persistedPage = assertNotNull(result.data)
        assertEquals(idToPick, persistedPage.id)
    }

}
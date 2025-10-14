package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.query.deferredQuery
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.switchStatement
import po.exposify.scope.sequence.launcher.launch
import po.exposify.scope.sequence.runtime.pickById
import po.exposify.scope.sequence.runtime.update
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPage
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withContentBlocks
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPick :
    DatabaseTest(),
    TasksManaged {
    override val identity: CTXIdentity<TestPick> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0

        @JvmStatic
        var persistedPages: MutableList<Page> = mutableListOf()
    }

    @BeforeAll
    fun setup() {
        logHandler.notifierConfig {
            setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
        }
        withConnection {
            service(UserDTO) {
                updatedById = update(mockedUser).dataUnsafe.id
            }
        }

        withConnection {
            val pages: List<Page> = mockPages(updatedById, quantity = 10) {
                name = "page_$it"
            }

            service(PageDTO) {
                val result = insert(pages)
                persistedPages.addAll(result.data)
                sequenced(PageDTO.Pick) { handler ->
                    pickById(handler) {

                    }
                }
            }
        }
    }

    @AfterAll
    fun cleanResults() {
        persistedPages.clear()
    }

    @Test
    fun `Sequenced PICK by id statement`(): TestResult = runTest {
        val idToPick = 1L

        val result = with(mockedSession) {
            launch(PageDTO.Pick, idToPick)
        }

        assertIs<ResultSingle<PageDTO, Page>>(result)
        assertTrue(!result.isFaulty, "Pick completed with error")
        assertNull(result.failureCause, "Pick completed with error")
        val persistedPage = assertNotNull(result.data, "PickById failed")
        val pageDTO = assertNotNull(result.dto)
        println(pageDTO)

        assertEquals(idToPick, pageDTO.id, "Picked wrong dto")
        assertEquals(idToPick, persistedPage.id, "Page id not updated")
    }

}

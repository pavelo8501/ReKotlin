package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.switchDTO
import po.exposify.scope.sequence.inputs.ParameterInput
import po.exposify.scope.sequence.inputs.withInput
import po.exposify.scope.sequence.launcher.launchSwitching
import po.exposify.scope.sequence.runtime.pickById
import po.exposify.scope.sequence.runtime.update
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.ContentBlock
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockSection
import po.test.exposify.setup.mocks.mockSections
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSwitchUpdate: DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestSwitchUpdate> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0
        @JvmStatic
        var persistedPages: MutableList<Page> =  mutableListOf()
    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }

        withConnection {
            val pages: List<Page> = mockPages(updatedBy = updatedById,  quantity = 10) { name = "page_$it" }

            service(PageDTO) {

                persistedPages.addAll(insert(pages).data)

                sequenced(PageDTO.Pick) { handler ->
                    pickById(handler) {
                        switchDTO(SectionDTO.Update) { switchHandler ->
                            update(switchHandler) {

                            }
                        }
                        switchDTO(SectionDTO.UpdateList){listHandler ->
                            update(listHandler){

                            }
                        }
                    }
                }
            }
        }
    }

    @AfterAll
    fun cleanResults(){
        persistedPages.clear()
    }

    @Test
    fun `Sequenced PICK with switch statement single input`(): TestResult = runTest {
        assertTrue(!persistedPages.isEmpty())
        val page = assertNotNull(persistedPages.firstOrNull { it.id == 8L })

        val section = mockSection( page.id, "Mocked Session", "Mocked Session", updatedById)
        val result = with(mockedSession) {
            launchSwitching(SectionDTO.Update, section, ParameterInput(page.id, PageDTO.Pick))
        }
        assertTrue(!result.isFaulty)
    }

    @Test
    fun `Sequenced PICK with switch statement multi input`(): TestResult = runTest {
        val page = assertNotNull(persistedPages.firstOrNull { it.id == 8L })
        val sections =  mockSections(page, 10){index->
            name = "section_$index"
            description = "Mocked Session # $index"
        }

        val result = with(mockedSession) {
            launchSwitching(SectionDTO.UpdateList, sections, ParameterInput(page.id, PageDTO.Pick))
        }
        assertTrue(!result.isFaulty)
    }

    @Test
    fun `Sequenced PICK with switch statement multi input and nested creation`(): TestResult = runTest {
        val page = assertNotNull(persistedPages.firstOrNull { it.id == 8L })

        val sections =  mockSections(page, 2) { index ->
            name = "section_$index"
            description = "Mocked Section # $index"

            withContentBlocks(2){index ->
                name = "contentBlock_$index"
                description = "Mocked ContentBlocks # $index"
            }
        }
        assertTrue(sections.firstOrNull()?.contentBlocks?.isNotEmpty()?:false)

        val result = with(mockedSession) {
            launchSwitching(SectionDTO.UpdateList, sections, withInput(8L, PageDTO.Pick))
        }

        assertTrue(!result.isFaulty)
        assertEquals(2, result.dto.size)

        val contentBlock = assertNotNull(result.data.flatMap { it.contentBlocks}.lastOrNull())
        assertIs<ContentBlock>(contentBlock)
        assertNotEquals(0, contentBlock.id)
    }

}
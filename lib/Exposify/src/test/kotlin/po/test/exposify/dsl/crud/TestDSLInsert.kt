package po.test.exposify.dsl.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import po.exposify.scope.launchers.pick
import po.exposify.scope.launchers.insert
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.newMockedSession
import po.test.exposify.setup.mocks.withContentBlocks
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestDSLInsert: DatabaseTest() {


    override val identity = asIdentity()

    companion object {
        @JvmStatic
        lateinit var updatedBy: User
    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO) {
                updatedBy = update(mockedUser).dataUnsafe
            }
            service(PageDTO) {

            }
        }
    }

    @Test
    @Order(1)
    fun `Insert statement with deep nesting`(): TestResult = runTest(timeout = Duration.parse("600s")) {
        val pages = mockPages(updatedBy.id, 2) {
            withSections(2) {
                withContentBlocks(1)
            }
        }

        val result = with(newMockedSession) {
            insert(PageDTO, pages)
        }
        val persistedPages = result.dataUnsafe
        assertEquals(2, persistedPages.size)

        val lastInputSection = pages.last().sections.last()
        assertNotEquals(0L, lastInputSection.updatedBy)

        val lastInputBlock = lastInputSection.contentBlocks.last()

        val lastPersistedPage = assertNotNull(persistedPages.lastOrNull())
        assertNotEquals(0L, lastPersistedPage.id)
        assertEquals(2, lastPersistedPage.sections.size, "Sections not persisted")

        val lastPersistedSection = assertNotNull(lastPersistedPage.sections.lastOrNull())
        assertEquals(lastInputSection.updatedBy, lastPersistedSection.updatedBy)

        assertEquals(lastPersistedPage.id, lastPersistedSection.pageId, "Page id not updated")

        val lastPersistedBlock = assertNotNull(lastPersistedSection.contentBlocks.lastOrNull())
        assertEquals(lastPersistedSection.id, lastPersistedBlock.sectionId)
        assertEquals(lastInputBlock.name, lastPersistedBlock.name)
        assertEquals(lastInputBlock.content, lastPersistedBlock.content)
    }

    @Test
    @Order(2)
    fun `Insert statement run from pick lambda`(): TestResult = runTest(timeout = Duration.parse("600s")) {
        val pickId = 1L
        lateinit var inputSections: List<Section>
        val result = with(newMockedSession) {
            pick(PageDTO, pickId) {
                dataUnsafe.withSections(2) {index->
                    description = "section_${index}_on_persisted_page${pickId}"
                }
                inputSections = dataUnsafe.sections.filter { it.id == 0L }
                insert(SectionDTO, inputSections)
            }
        }
        val sections = result.dataUnsafe
        assertEquals(2, sections.size)

        val lastInputSection =  inputSections.last()
        val lastInserted = assertNotNull(sections.lastOrNull())
        assertEquals(pickId, lastInserted.pageId)
        assertEquals(updatedBy.id, lastInserted.updatedBy)
        assertEquals(lastInputSection.name, lastInserted.name)
        assertEquals(lastInputSection.description, lastInserted.description)
    }


}
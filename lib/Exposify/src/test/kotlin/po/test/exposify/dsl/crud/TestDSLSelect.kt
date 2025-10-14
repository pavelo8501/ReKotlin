package po.test.exposify.dsl.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.scope.launchers.pick
import po.exposify.scope.launchers.select
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
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
class TestDSLSelect: DatabaseTest() {

    override val identity: CTXIdentity<TestDSLSelect> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0
    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO) {
                updatedById = update(mockedUser).dataUnsafe.id
            }
        }
        val pages = mockPages(updatedById, 3) {
            withSections(2){
                withContentBlocks(2)
            }
        }

        withConnection {
            service(PageDTO) {
                insert(pages)
            }
        }
    }

    @Test
    fun `Select statement`() = runTest(timeout = Duration.parse("600s")) {

        PageDTO.clearCachedDTOs()
        val result = with(newMockedSession) {
            select(PageDTO)
        }
        assertEquals(3, result.data.size)

        val page = assertNotNull(result.data.firstOrNull())
        assertNotEquals(0, page.id)
        assertEquals(updatedById, page.updatedBy, "User id update failure for Page data")
        assertEquals(2, page.sections.size)

        val lastSection = assertNotNull(page.sections.lastOrNull())
        assertNotEquals(0, lastSection.id)
        assertEquals(updatedById, lastSection.updatedBy, "User update failure")
        assertEquals(1L, lastSection.pageId, "Page reference update failure")

        assertEquals(2, lastSection.contentBlocks.size)
        val lastContent = assertNotNull(lastSection.contentBlocks.lastOrNull())


    }

    fun `Select statement folowed after PickById`() = runTest(timeout = Duration.parse("600s")) {

        val result = with(newMockedSession) {
            pick(PageDTO, 1L) {
                select(SectionDTO)
            }
        }
        assertEquals(2, result.data.size)
        val lastSection = assertNotNull(result.data.lastOrNull())
        assertNotEquals(0, lastSection.id)
        assertEquals(updatedById, lastSection.updatedBy, "User update failure")
        assertEquals(1L, lastSection.pageId, "Page reference update failure")

        assertEquals(2, lastSection.contentBlocks.size, "ContentBlocks not selected")

        val lastContentBlock = assertNotNull(lastSection.contentBlocks.lastOrNull())
        assertEquals(lastSection.id, lastContentBlock.sectionId, "Id update failure")

    }
}
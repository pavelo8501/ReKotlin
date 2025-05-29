package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.result.ResultList
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.scope.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pagesSectionsContentBlocks
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestComplexDelegate : DatabaseTest() {


    companion object{
        @JvmStatic()
        var userId : Long = 0
    }

    @BeforeAll
    fun setup() = runTest{
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection{
            service(UserDTO, TableCreateMode.CREATE) {
                userId =  update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `parent2IdReference property binding on update`() = runTest {

        val sourceSections = sectionsPreSaved(0)
        val page = pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 3, contentBlocksCount = 1, updatedBy = userId).first()
        var updatedPageData: Page? = null
        var updatedPageDTO : PageDTO? = null
        startTestConnection{
            service(PageDTO, TableCreateMode.CREATE) {
                page.sections.addAll(sourceSections)
                val updateResult = update(page)
                updatedPageData = updateResult.getDataForced()
                updatedPageDTO = updateResult.getDTOForced()
            }
        }
        val persistedDataSection  = updatedPageData?.sections?.first()
        val persistedFirstDtoSection = updatedPageDTO?.sections?.first()
        val persistedLastDtoSection = updatedPageDTO?.sections?.last()

        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedFirstDtoSection, "Failed to get DtoSection after update")
        assertNotNull(persistedLastDtoSection, "Failed to get DtoSection after update")
        assertNotEquals(persistedFirstDtoSection, persistedLastDtoSection, "First and last sections(DTO) are the same")

        assertAll("idReferenced updated in dto",
            { assertNotEquals(0, updatedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPageData.id, persistedDataSection.pageId, "PageId in data model not updated. Expected ${updatedPageData.id}") },
            { assertEquals(updatedPageData.id, persistedFirstDtoSection.pageId, "PageId in DTO not updated.. Expected ${updatedPageData.id}") },
            { assertEquals(updatedPageData.id, persistedLastDtoSection.pageId, "PageId in last picked section. Expected ${updatedPageData.id}") }
          )

        val persistedFirstSection = updatedPageData.sections.first()
        val persistedLastSection = updatedPageData.sections.last()

        assertNotEquals(persistedFirstSection, persistedLastSection, "First and last section(Data Model) are the same")

        assertAll("idReferenced updated in data model",
            { assertNotEquals(0, updatedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPageData.id, persistedFirstSection.pageId, "PageId in first section not updated. Expected ${updatedPageData.id}") },
            { assertEquals(updatedPageData.id, persistedLastSection.pageId, "PageId in last section  not updated.. Expected ${updatedPageData.id}") },
        )
    }

    @Test
    fun `foreign2IdReference property binding on select`() = runTest{

        val page = pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 3, contentBlocksCount = 1, updatedBy = userId).first()

        lateinit var selectedResult : ResultList<PageDTO, Page, PageEntity>

        startTestConnection{
            service(PageDTO, TableCreateMode.CREATE) {
                update(page)
                selectedResult = select()
            }
        }


        val persistedPageDto = assertNotNull(selectedResult.getDTO().firstOrNull(), "Page(DTO) is null")
        val persistedFirstDtoSection = assertNotNull(persistedPageDto.sections.firstOrNull(), "Section(DTO) is null")
        val persistedLastDtoSection = assertNotNull(persistedPageDto.sections.lastOrNull(), "Section(DTO) is null")
        assertNotEquals(persistedFirstDtoSection, persistedLastDtoSection, "First and last Sections(DTO) are the same")

        assertAll("page_id selected appropriately",
            { assertNotEquals(0, persistedPageDto.id, "id updated on select in PageDTO")},
            { assertEquals(persistedPageDto.id, persistedFirstDtoSection.pageId, "PageId mismatch in first selected Section(DTO)")},
            { assertEquals(persistedPageDto.id, persistedLastDtoSection.pageId, "PageId mismatch in last selected Section(DTO)")}
        )
    }


    @Test
    fun `foreign2IdReference property binding on update&pick`() = runTest{


        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1)
        var updatedPageData: Page? = null
        var updatedPageDTO : PageDTO? = null
        var pickedData: Page? = null
        var pickedDTO : PageDTO? = null
        startTestConnection {
            service(PageDTO, TableCreateMode.CREATE) {
                page.sections.addAll(sourceSections)
                val updateResult = update(page)
                updatedPageData = updateResult.getDataForced()
                updatedPageDTO = updateResult.getDTO() as PageDTO
                val selectionResult = pickById(updatedPageData.id)
                pickedData = selectionResult.getDataForced()
                pickedDTO = selectionResult.getDTO() as PageDTO
            }
        }

        assertNotNull(updatedPageData, "Failed to update PageData")
        assertNotNull(updatedPageDTO, "Failed  to update PageDTO")

//        assertAll("foreign2IdReference updated",
//            { assertNotEquals(0, userId, "updatedPageData id failed to update") },
//            { assertEquals(userId, updatedPageData.updatedById, "UpdatedById in data model not updated. Expected ${userId}") },
//            { assertEquals(userId, updatedPageDTO.updatedById, "UpdatedById in DTO not updated.. Expected ${userId}") },
//        )

        assertNotNull(pickedData, "Failed to pick PageData")
        assertNotNull(pickedDTO, "Failed  to pick PageDTO")

//        assertAll("foreign2IdReference picked",
//            { assertEquals(
//                updatedPageData.updatedById,
//                pickedData.updatedById,
//                "UpdatedById in data model not updated. Expected ${updatedPageData.updatedById}") },
//            { assertEquals(
//                updatedPageData.updatedById,
//                pickedData.updatedById,
//                "UpdatedById in DTO not updated.. Expected $updatedPageData.updatedById}") },
//        )
    }
}
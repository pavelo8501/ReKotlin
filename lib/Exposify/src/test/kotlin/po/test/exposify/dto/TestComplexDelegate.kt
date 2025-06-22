package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.result.ResultList
import po.exposify.scope.service.enums.TableCreateMode
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
        withConnection{
            service(UserDTO, TableCreateMode.CREATE) {
                userId =  update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `parentReference property binding`(){
        val sourceSections = sectionsPreSaved(0)
        val page = pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 3, contentBlocksCount = 1, updatedBy = userId).first()
        page.sections.addAll(sourceSections)
        lateinit var updatedPage: Page
        lateinit var updatedPageDTO : PageDTO
        withConnection{
            service(PageDTO, TableCreateMode.CREATE){
                val updateResult = update(page)
                updatedPage = updateResult.getDataForced()
                updatedPageDTO = updateResult.getDTOForced()
            }
        }

        val sectionDtoFirst = assertNotNull(updatedPageDTO.sections.firstOrNull(), "Failed to get DtoSection after update")
        val sectionDtoLast = assertNotNull(updatedPageDTO.sections.lastOrNull(), "Failed to get DtoSection after update")
        assertNotEquals(sectionDtoFirst, sectionDtoLast, "First and last sections(DTO) are the same")

        assertAll("idReferenced updated in dto",
            { assertNotEquals(0, updatedPage.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPage.id, sectionDtoFirst.page.id, "PageId in DTO not updated.. Expected ${updatedPage.id}") },
            { assertEquals(updatedPage.id, sectionDtoLast.page.id, "PageId in last picked section. Expected ${updatedPage.id}") }
          )

        val firstSection = assertNotNull(updatedPage.sections.firstOrNull(), "firstSection is null")
        val lastSection = assertNotNull(updatedPage.sections.lastOrNull(), "lastSection is null" )

        assertNotEquals(firstSection, lastSection, "First and last section(Data Model) are the same")

        assertAll("idReferenced updated in data model",
            { assertNotEquals(0, updatedPage.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPage.id, firstSection.pageId, "PageId in first section not updated. Expected ${updatedPage.id}") },
            { assertEquals(updatedPage.id, lastSection.pageId, "PageId in last section  not updated.. Expected ${updatedPage.id}") },
        )
    }

    @Test
    fun `parentReference  property binding on select`() = runTest{
        val page = pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 3, contentBlocksCount = 1, updatedBy = userId).first()
        lateinit var selectedResult : ResultList<PageDTO, Page, PageEntity>
        withConnection{
            service(PageDTO, TableCreateMode.CREATE) {
                update(page)
                selectedResult = select()
            }
        }

        val persistedPageDto = assertNotNull(selectedResult.getDTO().firstOrNull(), "Page(DTO) is null")
        val sectionDTOFirst = assertNotNull(persistedPageDto.sections.firstOrNull(), "Section(DTO) is null")
        val sectionDTOLast = assertNotNull(persistedPageDto.sections.lastOrNull(), "Section(DTO) is null")

        assertNotEquals(sectionDTOFirst, sectionDTOLast, "First and last Sections(DTO) are the same")

        assertAll("page_id selected appropriately",
            { assertNotEquals(0, persistedPageDto.id, "id updated on select in PageDTO")},
            { assertEquals(persistedPageDto.id, sectionDTOFirst.page.id, "PageId mismatch in first selected Section(DTO)")},
            { assertEquals(persistedPageDto.id, sectionDTOLast.page.id, "PageId mismatch in last selected Section(DTO)")}
        )
    }


    @Test
    fun `attachedReference property binding on update&pick`() = runTest{

        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedBy = userId)
        page.sections.addAll(sourceSections)
        var updatedPageData: Page? = null
        var pickedData: Page? = null
        lateinit var pickedDTO : PageDTO
        withConnection {
            service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                val updated = update(page)
                updatedPageData = updated.getDataForced()
                val picked = pickById(updatedPageData.id)
                pickedData = picked.getDataForced()
                pickedDTO = picked.getDTOForced()
            }
        }
        assertNotNull(updatedPageData, "Failed to update PageData")
        assertNotNull(pickedData, "Failed to pick PageData")
        assertNotNull(pickedDTO, "Failed  to pick PageDTO")

        assertAll("attachedReference picked",
            { assertEquals(
                updatedPageData.updatedBy,
                pickedData.updatedBy,
                "UpdatedById in data model not updated. Expected ${updatedPageData.updatedBy}") },
            { assertEquals(
                updatedPageData.updatedBy,
                pickedData.updatedBy,
                "UpdatedById in DTO not updated.. Expected $updatedPageData.updatedById}") },
        )
    }
}
package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestComplexDelegate : DatabaseTest() {


    @Test
    fun `parent2IdReference property binding un update`() = runTest {

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection(){
            service(UserDTO) {
                user = update(user).getDataForced()
            }
        }

        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
        var updatedPageData: Page? = null
        var updatedPageDTO : PageDTO? = null
        startTestConnection{
            service(PageDTO, TableCreateMode.CREATE) {
                page.sections.addAll(sourceSections)
                val updateResult = update(page)
                updatedPageData = updateResult.getDataForced()
                updatedPageDTO = updateResult.getDTO() as PageDTO
            }
        }
        val persistedDataSection  = updatedPageData?.sections?.first()
        val persistedDtoSection = updatedPageDTO?.sections?.first() as? SectionDTO

        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedDtoSection, "Failed to get DtoSection after update")

        assertAll("idReferenced updated",
            { assertNotEquals(0, updatedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(updatedPageData.id, persistedDataSection.pageId, "PageId in data model not updated. Expected ${updatedPageData.id}") },
            { assertEquals(updatedPageData.id, persistedDtoSection.pageId, "PageId in DTO not updated.. Expected ${updatedPageData.id}") },
          )
    }

    @Test
    fun `parent2IdReference property binding un select`() = runTest {

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection(){
            service(UserDTO) {
                user = update(user).getDataForced()
            }
        }

        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
        var selectedData: Page? = null
        var selectedDTO : PageDTO? = null
        var updatedSectionsCount = 0

        startTestConnection() {
            service(PageDTO, TableCreateMode.CREATE) {
                page.sections.addAll(sourceSections)
                updatedSectionsCount = update(page).getDataForced().sections.count()

                val selectionResult = select()

                selectedData = selectionResult.getData().firstOrNull()
                selectedDTO = selectionResult.getDTO().firstOrNull() as? PageDTO
            }
        }

        val selectedPageData = assertNotNull(selectedData)
        val selectedPageDTO = assertNotNull(selectedDTO)

        val persistedDataSection  = selectedPageData.sections.firstOrNull()
        val persistedDtoSection = selectedPageDTO.sections.firstOrNull() as? SectionDTO

        assertNotEquals(0, updatedSectionsCount, "Sections update failed. Expected count ${page.sections.count()}")
        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedDtoSection, "Failed to get DtoSection after update")

        assertAll("idReferenced updated",
            { assertNotEquals(0, selectedPageData.id, "updatedPageData id failed to update") },
            { assertEquals(
                selectedPageData.id,
                persistedDataSection.pageId,
                "PageId in data model not updated. Expected ${selectedPageData.id}") },
            { assertEquals(
                selectedPageData.id,
                persistedDtoSection.pageId,
                "PageId in DTO not updated. Expected ${selectedPageData.id}") },
        )
    }


    @Test
    fun `foreign2IdReference property binding un update&pick`() = runTest{

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        startTestConnection(){
           service(UserDTO) {
                user = update(user).getDataForced()
            }
        }

        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
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

        assertAll("foreign2IdReference updated",
            { assertNotEquals(0, user.id, "updatedPageData id failed to update") },
            { assertEquals(user.id, updatedPageData.updatedById, "UpdatedById in data model not updated. Expected ${user.id}") },
            { assertEquals(user.id, updatedPageDTO.updatedById, "UpdatedById in DTO not updated.. Expected ${user.id}") },
        )

        assertNotNull(pickedData, "Failed to pick PageData")
        assertNotNull(pickedDTO, "Failed  to pick PageDTO")

        assertAll("foreign2IdReference picked",
            { assertEquals(
                updatedPageData.updatedById,
                pickedData.updatedById,
                "UpdatedById in data model not updated. Expected ${updatedPageData.updatedById}") },
            { assertEquals(
                updatedPageData.updatedById,
                pickedData.updatedById,
                "UpdatedById in DTO not updated.. Expected $updatedPageData.updatedById}") },
        )
    }
}
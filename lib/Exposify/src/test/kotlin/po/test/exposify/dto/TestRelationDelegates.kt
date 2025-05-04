package po.test.exposify.dto

import kotlinx.coroutines.test.runTest
import org.junit.Test
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
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TestRelationDelegates : DatabaseTest() {


    @Test
    fun `oneToManyOf relation delegate when selecting`() = runTest {

        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()
        connection?.service(UserDTO) {
            user = update(user).getData()
        }

        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)


        var selectedData: Page? = null
        var selectedDTO : PageDTO? = null
        var updatedSectionsCount = 0

        connection?.service(PageDTO, TableCreateMode.CREATE) {
            page.sections.addAll(sourceSections)
            updatedSectionsCount = update(page).getData().sections.count()
            val selectionResult = select()
            selectedData = selectionResult.getData().firstOrNull()
            selectedDTO = selectionResult.getDTO().firstOrNull() as? PageDTO
        }

        val selectedPageData = assertNotNull(selectedData)
        val selectedPageDTO = assertNotNull(selectedDTO)

        val persistedDataSection  = selectedPageData.sections.firstOrNull()
        val persistedDtoSection = selectedPageDTO.sections.firstOrNull() as? SectionDTO

        assertNotEquals(0, updatedSectionsCount, "Sections update failed. Expected count ${page.sections.count()}")
        assertNotNull(persistedDataSection, "Failed to get DataSection after update")
        assertNotNull(persistedDtoSection, "Failed to get DtoSection after update")

    }


    @Test
    fun `oneToManyOf relation delegate when updating`(){
        var user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        val connection = startTestConnection()
        connection?.service(UserDTO) {
            user = update(user).getData()
        }
        val sourceSections = sectionsPreSaved(0)
        val page = Page(id = 0, name = "home", langId = 1, updatedById = user.id)
        var updatedPageData: Page? = null
        var updatedPageDTO : PageDTO? = null
        connection?.service(PageDTO, TableCreateMode.CREATE) {
            page.sections.addAll(sourceSections)
            val updateResult = update(page)
            updatedPageData = updateResult.getData()
            updatedPageDTO = updateResult.getDTO() as PageDTO
        }

        assertNotNull(updatedPageData, "Failed to get data model after update")
        assertNotNull(updatedPageDTO, "Failed to get dto after update")

        assertAll("Sections Updated",
            { assertEquals(
                sourceSections.count(),
                updatedPageData.sections.count(),
                "Sections count mismatch in data model") },
            { assertEquals(
                sourceSections.count(),
                updatedPageDTO.sections.count(),
                "Sections count mismatch in dto model") },
            )
        val originalSection  = sourceSections.first()
        val persistedDataSection  = updatedPageData.sections.first()
        val persistedDtoSection = updatedPageDTO.sections.first() as? SectionDTO

        assertNotNull(persistedDataSection)
        assertNotNull(persistedDtoSection)
        assertAll("ContentBlocks Updated",
            { assertEquals(
                originalSection.contentBlocks.count(),
                persistedDataSection.contentBlocks.count(),
                "ContentBlocks count mismatch in data model") },
            { assertEquals(originalSection.contentBlocks.count(),
                persistedDtoSection.contentBlocks.count(),
                "ContentBlocks count mismatch in dto model") },
        )
    }

}
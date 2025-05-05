package po.test.exposify.crud

import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.WhereCondition
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestSelect : DatabaseTest() {

    @Test
    fun `referenced property binding`(){
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        val pages = pageModels(pageCount = 1, updatedBy = 1)
        var assignedUserId : Long = 0
        startTestConnection()?.let {connection->
            connection.service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                val userData =  update(user).getData()
                assignedUserId = userData.id
                assertEquals("some_login", userData.login, "User login mismatch after save")
                assertNotEquals(0, assignedUserId, "User id assignment failure")
            }

            connection.service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                val pageData =  update(pages[0]).getData()
                val updatedById =  pageData.updatedById

                assertEquals(assignedUserId, updatedById, "Failed to update DTOs referenced property")
            }
        }
    }

    @Test
    fun `postgres serializable classes`(){
        val pages = pageModels(
            pageCount = 1,
            updatedBy = 1)

        startTestConnection()?.let {connection->

            connection.service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                val originalPages = pages[0]
                val updatedPageDtos = update(pages)
                val updatePagesData = updatedPageDtos.getData()
                val selectedPageDtos = select()
                val selectedPagesData = selectedPageDtos.getData()
            }
        }
    }

    @Test
    fun `updates and selects DTO relations`(){
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        var assignedUserId : Long = 0
        startTestConnection()?.let {connection->
            connection.service(UserDTO, TableCreateMode.CREATE) {
                val userData =  update(user).getData()
                assignedUserId = userData.id
            }
            val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = assignedUserId)
            connection.service(PageDTO.Companion, TableCreateMode.CREATE) {

                val originalPage = pages[0]
                val originalSection = originalPage.sections[0]

                val updatedPages = update(pages).getData()
                val updatedPage = updatedPages[0]
                val updatedSection = updatedPage.sections[0]

                assertAll(
                    { assertEquals(1, updatedPages.count(), "Page count mismatch") },
                    { assertNotEquals(0, updatedPages[0].id, "Updated page id assignment failure") },
                    { assertEquals(originalPage.name, updatedPage.name, "Page name mismatch") },
                    { assertEquals(originalPage.langId, updatedPage.langId, "Page langId mismatch") },
                    {
                        assertEquals(
                            originalPage.sections.count(),
                            updatedPage.sections.count(),
                            "Section count mismatch"
                        )
                    },
                    { assertNotEquals(0, updatedSection.id, "Updated section id assignment failure") },
                    { assertEquals(originalSection.name, updatedSection.name, "Section name mismatch") },
                    { assertEquals(originalSection.langId, updatedSection.langId, "Section langId mismatch") }
                )

                val selectedPageDtos = select()
                val selectedPage = selectedPageDtos.getData().last()

                assertNotEquals(selectedPage.sections.count(), 0, "Selected sections  0")
                val selectedSection = selectedPage.sections[0]
                assertAll(
                    { assertEquals(updatedPage.name, selectedPage.name, "Selected page name mismatch") },
                    { assertEquals(updatedPage.langId, selectedPage.langId, "Selected page langId mismatch") },
                    {
                        assertEquals(
                            updatedPage.sections.count(),
                            selectedPage.sections.count(),
                            "Selected sections count mismatch"
                        )
                    },
                    { assertEquals(updatedSection.name, selectedSection.name, "Selected section name mismatch") },
                    { assertEquals(updatedSection.langId, selectedSection.langId, "Selected section langId mismatch") },
                    { assertNotEquals(0, selectedSection.langId, "Selected section id not updated") }
                )
            }
        }
    }

    @Test
    fun `updates and selects with conditions`(){
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        var assignedUserId : Long = 0
        startTestConnection()?.let { connection ->
            connection.service(UserDTO, TableCreateMode.CREATE) {
                val userData = update(user).getData()
                assignedUserId = userData.id
            }
            val pages = pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = assignedUserId)
            pages[0].langId = 1
            pages[1].langId = 2
            connection.service(PageDTO.Companion, TableCreateMode.CREATE) {
                truncate()
                update(pages)
                val selectedPages =  select(WhereCondition<Pages>().equalsTo(Pages.langId, 1)).getData()
                assertEquals(1, selectedPages.count(), "Page count mismatch")
                val selectedSections = selectedPages[0].sections
                assertAll(
                    { assertEquals(2, selectedSections.count(), "Selected Sections Mismatch") },
                    { assertNotEquals(0, selectedSections[0].id, "Selected Section update failure") },
                    {
                        assertEquals(
                            assignedUserId,
                            selectedSections[0].updatedBy,
                            "Selected Section updated with wrong updatedBy"
                        )
                    }
                )
            }
        }
    }

}
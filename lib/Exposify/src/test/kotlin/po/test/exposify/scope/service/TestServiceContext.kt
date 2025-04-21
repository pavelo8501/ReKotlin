package po.test.exposify.scope.service

import org.junit.jupiter.api.assertAll
import po.exposify.extensions.WhereCondition
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.DatabaseTest
import po.test.exposify.setup.TestClassItem
import po.test.exposify.setup.TestPage
import po.test.exposify.setup.TestPageDTO
import po.test.exposify.setup.TestPages
import po.test.exposify.setup.TestUser
import po.test.exposify.setup.TestUserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestServiceContext : DatabaseTest() {

    @Test
    fun `referenced property binding`(){
        val user = TestUser("some_login", "name", "nomail@void.null", "******")
        val pages = pageModels(quantity =  1, updatedBy = 1)
        var assignedUserId : Long = 0
        connectionContext?.let {connection->
            connection.service(TestUserDTO, TableCreateMode.FORCE_RECREATE) {
               val userData =  update(user).getData()
               assignedUserId = userData.id
               assertEquals("some_login", userData.login, "User login mismatch after save")
               assertNotEquals(0, assignedUserId, "User id assignment failure")
            }

            connection.service(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                val pageData =  update(pages[0]).getData()
                val updatedById =  pageData.updatedById

                assertEquals(assignedUserId, updatedById, "Failed to update DTOs referenced property")
            }
        }
    }

    @Test
    fun `postgres serializable classes`(){
        val pages = pageModels(quantity = 1,  updatedBy =  1, pageClasses = listOf(TestClassItem(1,"class_1"), TestClassItem(2,"class_2")) )
        connectionContext?.let {connection->

            connection.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                val originalPages = pages[0]
                val updatedPageDtos = update(pages)
                val updatePagesData = updatedPageDtos.getData()
                val updatedPageData = updatePagesData[0]
                var firstPageClassRecord =  updatedPageData.pageClasses[0]
                assertEquals(originalPages.pageClasses.count(), updatedPageData.pageClasses.count(), "ClassList count mismatch")
                assertAll(
                    {assertEquals(1, firstPageClassRecord.key, "Page classList key mismatch in updated")},
                    {assertEquals("class_1", firstPageClassRecord.value, "Page classList value mismatch in updated")}
                )

                val selectedPageDtos = select()
                val selectedPagesData = selectedPageDtos.getData()
                val selectedPagePageClasses = selectedPagesData[0].pageClasses
                assertEquals(originalPages.pageClasses.count(), selectedPagePageClasses.count(), "PageClasses  count mismatch")
                firstPageClassRecord =  selectedPagePageClasses[0]

                assertAll(
                    {assertEquals(1, firstPageClassRecord.key, "Page classList key mismatch in selection")},
                    {assertEquals("class_1", firstPageClassRecord.value, "Page classList value mismatch in selection")}
                )
            }
        }
    }

    @Test
    fun `updates and selects DTO relations`(){
        val user = TestUser("some_login", "name", "nomail@void.null", "******")
        var assignedUserId : Long = 0
        connectionContext?.let {connection->
            connection.service<TestUserDTO, TestUser>(TestUserDTO, TableCreateMode.CREATE) {
                val userData =  update(user).getData()
                assignedUserId = userData.id
            }
            val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 2,  updatedBy =  assignedUserId)
            connection.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.CREATE) {

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
                    { assertEquals(originalPage.sections.count(), updatedPage.sections.count(), "Section count mismatch") },
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
                    { assertEquals(updatedPage.sections.count(), selectedPage.sections.count(), "Selected sections count mismatch") },
                    { assertEquals(updatedSection.name, selectedSection.name, "Selected section name mismatch") },
                    { assertEquals(updatedSection.langId, selectedSection.langId, "Selected section langId mismatch")},
                    { assertNotEquals(0, selectedSection.langId, "Selected section id not updated")}
                )
            }
        }
    }

    @Test
    fun `updates and selects with conditions`(){
        val user = TestUser("some_login", "name", "nomail@void.null", "******")
        var assignedUserId : Long = 0
        connectionContext?.let { connection ->
            connection.service(TestUserDTO, TableCreateMode.CREATE) {
                val userData = update(user).getData()
                assignedUserId = userData.id
            }
            val pages = pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = assignedUserId)
            pages[0].langId = 1
            pages[1].langId = 2
            connection.service(TestPageDTO, TableCreateMode.CREATE) {
               truncate()
               update(pages)
               val selectedPages =  select(WhereCondition<TestPages>().equalsTo(TestPages.langId, 1)).getData()
               assertEquals(1, selectedPages.count(), "Page count mismatch")
               val selectedSections = selectedPages[0].sections
               assertAll(
                   {assertEquals(2,selectedSections.count(), "Selected Sections Mismatch")},
                   {assertNotEquals(0, selectedSections[0].id, "Selected Section update failure")},
                   {assertEquals(assignedUserId, selectedSections[0].updatedById, "Selected Section updated with wrong updatedBy")}
               )
            }
        }
    }

}
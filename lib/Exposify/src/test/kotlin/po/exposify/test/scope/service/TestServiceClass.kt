package po.exposify.test.scope.service

import org.junit.jupiter.api.assertAll
import po.exposify.scope.service.enums.TableCreateMode
import po.exposify.test.DatabaseTest
import po.exposify.test.setup.TestClassItem
import po.exposify.test.setup.TestPage
import po.exposify.test.setup.TestPageDTO
import po.exposify.test.setup.TestUser
import po.exposify.test.setup.TestUserDTO
import po.exposify.test.setup.pageModels
import po.exposify.test.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestServiceClass : DatabaseTest() {


    @Test
    fun `referenced property binding work`(){
        val user = TestUser("some_login", "name", "nomail@void.null", "******")
        val pages = pageModels(quantity =  1, updatedBy = 1)
        var assignedUserId : Long = 0
        connectionContext?.let {connection->
            connection.service<TestUserDTO, TestUser>(TestUserDTO, TableCreateMode.FORCE_RECREATE) {
               val userData =  update(user).getData()
               assignedUserId = userData.id
               assertEquals("some_login", userData.login, "User login mismatch after save")
               assertNotEquals(0, assignedUserId, "User id assignment failure")
            }

            connection.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
                val pageData =  update(pages[0]).getData()
                val updatedById =  pageData.updatedById

                assertEquals(assignedUserId, updatedById, "Failed to update DTOs referenced property")
            }

        }
    }

//    @Test
//    fun `postgres serializable classes work`(){
//        val testClassItems = listOf<TestClassItem>(TestClassItem(1,"class_1"), TestClassItem(2,"class_2"))
//        val pages = pageModels(quantity = 1, pageClasses = testClassItems, updatedBy =  1)
//
//        connectionContext?.let {
//            it.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
//                val originalPages = pages[0]
//                val updatedPageDtos = update(pages)
//                val updatePagesData = updatedPageDtos.getDataModels()
//                val updatedPageData = updatePagesData[0]
//                var firstPageClassRecord =  updatedPageData.pageClasses[0]
//                assertEquals(originalPages.pageClasses.count(), updatedPageData.pageClasses.count(), "ClassList count mismatch")
//                assertAll(
//                    {assertEquals(1, firstPageClassRecord.key, "Page classList key mismatch in updated")},
//                    {assertEquals("class_1", firstPageClassRecord.value, "Page classList value mismatch in updated")}
//                )
//
//                val selectedPageDtos = select()
//                val selectedPagesData = selectedPageDtos.getDataModels()
//                val selectedPagePageClasses = selectedPagesData[0].pageClasses
//                assertEquals(originalPages.pageClasses.count(), selectedPagePageClasses.count(), "PageClasses  count mismatch")
//                firstPageClassRecord =  selectedPagePageClasses[0]
//
//                assertAll(
//                    {assertEquals(1, firstPageClassRecord.key, "Page classList key mismatch in selection")},
//                    {assertEquals("class_1", firstPageClassRecord.value, "Page classList value mismatch in selection")}
//                )
//
//            }
//        }
//    }
//
//    @Test
//    fun `updates and selects DTO relations work`(){
//
//        val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 2,  updatedBy =  1)
//        connectionContext?.let {
//            it.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {
//
//                val originalPage = pages[0]
//                val originalSection = originalPage.sections[0]
//
//                val updatedPageDtos = update(pages)
//                val updatedPages = updatedPageDtos.getDataModels()
//                val updatedPage = updatedPages[0]
//                val updatedSection = updatedPage.sections[0]
//
//                assertAll(
//                    { assertEquals(1, updatedPages.count(), "Page count mismatch") },
//                    { assertNotEquals(0, updatedPages[0].id, "Updated page id assignment failure") },
//                    { assertEquals(originalPage.name, updatedPage.name, "Page name mismatch") },
//                    { assertEquals(originalPage.langId, updatedPage.langId, "Page langId mismatch") },
//                    { assertEquals(originalPage.sections.count(), updatedPage.sections.count(), "Section count mismatch") },
//                    { assertNotEquals(0, updatedSection.id, "Updated section id assignment failure") },
//                    { assertEquals(originalSection.name, updatedSection.name, "Section name mismatch") },
//                    { assertEquals(originalSection.langId, updatedSection.langId, "Section langId mismatch") }
//                )
//
//                val selectedPageDtos = select()
//                val selectedPages = selectedPageDtos.getDataModels()
//                val selectedPage = selectedPages[0]
//
//                assertNotEquals(selectedPage.sections.count(), 0, "Selected sections  0")
//                val selectedSection = selectedPage.sections[0]
//                assertAll(
//                    { assertEquals(updatedPage.name, selectedPage.name, "Selected page name mismatch") },
//                    { assertEquals(updatedPage.langId, selectedPage.langId, "Selected page langId mismatch") },
//                    { assertEquals(updatedPage.sections.count(), selectedPage.sections.count(), "Selected sections count mismatch") },
//                    { assertEquals(updatedSection.name, selectedSection.name, "Selected section name mismatch") },
//                    { assertEquals(updatedSection.langId, selectedSection.langId, "Selected section langId mismatch")},
//                    { assertNotEquals(0, selectedSection.langId, "Selected section id not updated")}
//                )
//            }
//        }
//    }

}
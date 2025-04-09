package po.exposify.test.scope.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertAll
import po.exposify.scope.service.enums.TableCreateMode
import po.exposify.test.DatabaseTest
import po.exposify.test.setup.TestPage
import po.exposify.test.setup.TestPageDTO
import po.exposify.test.setup.pageModelsWithSections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIsNot
import kotlin.test.assertNotEquals

class TestServiceClass : DatabaseTest() {

    @Test
    fun `service instantiates root dtos and insert table records`(){

        val pages = pageModelsWithSections(pageCount = 1, sectionsCount = 2)

        connectionContext?.let {
            it.service<TestPageDTO, TestPage>(TestPageDTO, TableCreateMode.FORCE_RECREATE) {

                val originalPage = pages[0]
                val originalSection = originalPage.sections[0]

                val updatedPageDtos = update(pages)
                val updatedPages = updatedPageDtos.getDataModels()
                val updatedPage = updatedPages[0]
                val updatedSection = updatedPage.sections[0]

                assertAll(
                    { assertEquals(1, updatedPages.count(), "Page count mismatch") },
                    { assertNotEquals(0, updatedPages[0].id, "Updated page id assignment failure")},
                    { assertEquals(originalPage.name, updatedPage.name, "Page name mismatch") },
                    { assertEquals(originalPage.langId, updatedPage.langId, "Page langId mismatch") },
                    { assertEquals(originalPage.sections.count(), updatedPage.sections.count(), "Section count mismatch") },
                    { assertNotEquals(0, updatedSection.id, "Updated section id assignment failure") },
                    { assertEquals(originalSection.name, updatedSection.name, "Section name mismatch") },
                    { assertEquals(originalSection.langId, updatedSection.langId, "Section langId mismatch") }
                )

                val selectedPages = select().getDataModels()
                val selectedPage = selectedPages[0]

                assertNotEquals(selectedPage.sections.count(), 0, "Selected sections > 0")
                val selectedSection = selectedPage.sections[0]
                assertAll(
                    { assertEquals(updatedPage.name, selectedPage.name, "Selected page name mismatch") },
                    { assertEquals(updatedPage.langId, selectedPage.langId, "Selected page langId mismatch") },
                    { assertEquals(updatedPage.sections.count(), selectedPage.sections.count(), "Selected sections count mismatch") },
                    { assertEquals(updatedSection.name, selectedSection.name, "Selected section name mismatch") },
                    { assertEquals(updatedSection.langId, selectedSection.langId, "Selected section langId mismatch") }
                )
            }
        }


      //  assertTrue(open)
    }
}
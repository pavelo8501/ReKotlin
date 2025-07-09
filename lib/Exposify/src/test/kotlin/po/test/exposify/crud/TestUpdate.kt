package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.common.events.ContextData
import po.exposify.common.events.DTOData
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.NotifyConfig
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pagesSectionsContentBlocks
import po.test.exposify.setup.sectionsPreSaved
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUpdate : DatabaseTest(), TasksManaged {

    override val contextName: String = "TestUpdate"

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }

    @BeforeAll
    fun setup() = runTest {

        logHandler.notifierConfig {
            console = NotifyConfig.ConsoleBehaviour.MuteNoEvents
            allowDebug(ContextData, DTOData)
        }
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `Saves new dto and verifies entire relation tree`(){
        val inputPage = pagesSectionsContentBlocks(pageCount = 1, sectionsCount =  2, contentBlocksCount = 2 , updatedBy = updatedById).first()
        inputPage.name = "TestPage"
        for(i in 0.. inputPage.sections.size-1){
            val index = i+1
            val section =  inputPage.sections[i]
            section.name = "Section_$index"

            for(a in 0 ..  section.contentBlocks.size-1){
                val indexA = a+1
                val contentBlock  = section.contentBlocks[a]
                contentBlock.name = "Content_${index}_$indexA"
            }
        }
        var updatedPage : Page? = null
        withConnection{
            service(PageDTO, TableCreateMode.Create) {
                updatedPage = update(inputPage).getDataForced()
            }
        }

        val  page = assertNotNull(updatedPage, "Updated page is null")
        val totalContBlocks = page.sections.sumOf { it.contentBlocks.size }
        assertAll("Page properly updated",
            { assertNotEquals(0, page.id, "Id did not updated") },
            { assertEquals("TestPage", page.name, "Name property update failed") },
            { assertEquals(updatedById, page.updatedBy, "Name property update failed") },
            { assertEquals(4,  totalContBlocks, "Sections count mismatch") }
        )

        val firstSection =  page.sections.first()
        val lastSection =  page.sections.last()

        assertNotSame(firstSection, lastSection, "First and last sections are the same object")

        assertAll("First section properly updated",
            { assertNotEquals(0, firstSection.id, "Id did not updated") },
            { assertEquals("Section_1", firstSection.name, "Name property update failed") },
            { assertEquals(updatedById, firstSection.updatedBy, "Name property update failed") },
            { assertTrue(firstSection.contentBlocks.size == 2, "ContentBlocks count mismatch") }
        )

        assertAll("Last section properly updated",
            { assertNotEquals(0, lastSection.id, "Id did not updated") },
            { assertEquals("Section_2", lastSection.name, "Name property update failed") },
            { assertEquals(updatedById, lastSection.updatedBy, "Name property update failed") },
            { assertTrue(lastSection.contentBlocks.size == 2, "ContentBlocks count mismatch") }
        )

        val firstContentBlock =  firstSection.contentBlocks.first()
        val lastContentBlock =  lastSection.contentBlocks.last()
        assertNotSame(firstContentBlock, lastContentBlock, "First and last content blocks are the same object")

        assertAll("First ContentBlock properly updated",
            { assertNotEquals(0, firstContentBlock.id, "Id did not updated") },
            { assertEquals("Content_1_1", firstContentBlock.name, "Name property update failed") },
        )

        assertAll("Last ContentBlock properly updated",
            { assertNotEquals(0, lastContentBlock.id, "Id did not updated") },
            { assertEquals("Content_2_2", lastContentBlock.name, "Name property update failed") },
        )
    }

    @Test
    fun `ParentReference property binding`(){
        val sourceSections = sectionsPreSaved(0)
        val page = pagesSectionsContentBlocks(pageCount = 1, sectionsCount = 3, contentBlocksCount = 1, updatedBy = updatedById).first()
        page.sections.addAll(sourceSections)
        lateinit var updatedPage: Page
        lateinit var updatedPageDTO : PageDTO
        withConnection{
            service(PageDTO, TableCreateMode.Create){
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
}
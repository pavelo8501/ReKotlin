package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pagesSectionsContentBlocks
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

        val loggerHandler: LogNotifyHandler  = logNotify()
        loggerHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
        }
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        withConnection {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
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
            service(PageDTO, TableCreateMode.CREATE) {
                updatedPage =  update(inputPage).getDataForced()

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


    fun `updates existent dto and verifies entire relation tree`() = runTest{

        val initialPages = pagesSectionsContentBlocks(pageCount = 1, sectionsCount =  1, contentBlocksCount = 1 , updatedBy = updatedById)
        var persistedPage : Page? = null
        var updateResult : ResultSingle<PageDTO, Page, PageEntity>? = null

        val updatedPageName = "other_name"
        val updatedSectionName = "other_section_name"
        val updatedContentBlockName = "other_content_block_name"

        withConnection {
            service(PageDTO, TableCreateMode.CREATE) {
                persistedPage =  update(initialPages[0]).getDataForced()
                persistedPage.name = updatedPageName
                persistedPage.sections[0].name =updatedSectionName
                persistedPage.sections[0].contentBlocks[0].name = updatedContentBlockName
                updateResult = update(persistedPage)
            }
        }

        val pageDto = assertNotNull(updateResult?.getDTO(),  "PageDto is null")
        assertAll("PageDto properly updated",
            { assertNotEquals(0, pageDto.id, "Id did not updated") },
            { assertEquals(updatedPageName, pageDto.name, "Name property update failed") },
            { assertTrue(pageDto.sections.size == 1, "Sections count mismatch") }
        )

        val sectionDto = assertNotNull(pageDto.sections[0],  "SectionDTO is null")
        assertAll("SectionDTO properly updated",
            { assertNotEquals(0, sectionDto.id, "Id did not updated") },
            { assertEquals(updatedSectionName, sectionDto.name, "Name property update failed") },
            { assertTrue(sectionDto.contentBlocks.size == 1, "ContentBlocks count mismatch") }
        )

        val contentBlockDTO = assertNotNull(pageDto.sections[0].contentBlocks[0],  "ContentBlockDTO is null")
        assertAll("ContentBlockDTO properly updated",
            { assertNotEquals(0, contentBlockDTO.id, "Id did not updated") },
            { assertEquals(updatedContentBlockName, contentBlockDTO.name, "Name property update failed") },
        )

        val page = assertNotNull(updateResult.getData(),  "Page is null")
        assertAll("Page properly updated",
            { assertNotEquals(0, page.id, "Id did not updated") },
            { assertEquals(updatedPageName, page.name, "Name property update failed") },
            { assertTrue(page.sections.size == 1, "Sections count mismatch") }
        )

        val section = assertNotNull(page.sections[0],  "Section is null")
        assertAll("Section properly updated",
            { assertNotEquals(0, section.id, "Id did not updated") },
            { assertEquals(updatedSectionName, section.name, "Name property update failed") },
            { assertTrue(section.contentBlocks.size == 1, "ContentBlocks count mismatch") }
        )

        val contentBlock = assertNotNull(page.sections[0].contentBlocks[0],  "ContentBlock is null")
        assertAll("ContentBlock properly updated",
            { assertNotEquals(0, contentBlock.id, "Id did not updated") },
            { assertEquals(updatedContentBlockName,  contentBlock.name, "Name property update failed") },
        )
    }


    fun `user updates`() = runTest {
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        var userDataModel: User? = null


        withConnection{
            service(UserDTO, TableCreateMode.CREATE){
               userDataModel = update(user).getData()
            }
        }

        val updatedUser = assertNotNull(userDataModel, "Updated data model is null")
        assertNotNull(updatedUser, "User Update failure")
        assertAll("User Properties Updates",
            { assertEquals(user.login, updatedUser.login, "Login property update failure. Should be: ${user.login}") },
            { assertNotEquals("password", updatedUser.hashedPassword, "Login property update failure. Should be: ${user.login}") }
        )
    }
}
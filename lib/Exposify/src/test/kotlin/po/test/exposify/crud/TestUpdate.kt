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
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUpdate : DatabaseTest(), TasksManaged {

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
        startTestConnection {
            service(UserDTO, TableCreateMode.FORCE_RECREATE) {
                updatedById = update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `saves new dto and verifies entire relation tree`() = runTest{

        val inputPages = pagesSectionsContentBlocks(pageCount = 1, sectionsCount =  1, contentBlocksCount = 1 , updatedBy = updatedById)
        val expectedContentBlock = inputPages[0].sections[0].contentBlocks[0]
        var updateResult : ResultSingle<PageDTO, Page, PageEntity>? = null

        startTestConnection{
            service(PageDTO, TableCreateMode.CREATE) {
                updateResult =  update(inputPages[0])
            }
        }

        val pageDto = assertNotNull(updateResult?.getDTO(),  "PageDto is null")
        assertAll("PageDto properly updated",
            { assertNotEquals(0, pageDto.id, "Id did not updated") },
            { assertEquals(inputPages[0].name, pageDto.name, "Name property update failed") },
            { assertTrue(pageDto.sections.size == 1, "Sections count mismatch") }
        )

        val sectionDto = assertNotNull(pageDto.sections[0],  "SectionDTO is null")
        assertAll("SectionDTO properly updated",
            { assertNotEquals(0, sectionDto.id, "Id did not updated") },
            { assertEquals(inputPages[0].sections[0].name, sectionDto.name, "Name property update failed") },
            { assertTrue(sectionDto.contentBlocks.size == 1, "ContentBlocks count mismatch") }
        )

        val contentBlockDTO = assertNotNull(pageDto.sections[0].contentBlocks[0],  "ContentBlockDTO is null")
        assertAll("ContentBlockDTO properly updated",
            { assertNotEquals(0, contentBlockDTO.id, "Id did not updated") },
            { assertEquals(expectedContentBlock.name, contentBlockDTO.name, "Name property update failed") },
        )

        val page = assertNotNull(updateResult.getData(),  "Page is null")
        assertAll("Page properly updated",
            { assertNotEquals(0, page.id, "Id did not updated") },
            { assertEquals(inputPages[0].name, page.name, "Name property update failed") },
            { assertTrue(page.sections.size == 1, "Sections count mismatch") }
        )

        val section = assertNotNull(page.sections[0],  "Section is null")
        assertAll("Section properly updated",
            { assertNotEquals(0, section.id, "Id did not updated") },
            { assertEquals(inputPages[0].sections[0].name, section.name, "Name property update failed") },
            { assertTrue(section.contentBlocks.size == 1, "ContentBlocks count mismatch") }
        )

        val contentBlock = assertNotNull(page.sections[0].contentBlocks[0],  "ContentBlock is null")
        assertAll("ContentBlock properly updated",
            { assertNotEquals(0, contentBlock.id, "Id did not updated") },
            { assertEquals(expectedContentBlock.name, contentBlock.name, "Name property update failed") },
        )
    }

    @Test
    fun `updates existent dto and verifies entire relation tree`() = runTest{

        val initialPages = pagesSectionsContentBlocks(pageCount = 1, sectionsCount =  1, contentBlocksCount = 1 , updatedBy = updatedById)
        var persistedPage : Page? = null
        var updateResult : ResultSingle<PageDTO, Page, PageEntity>? = null

        val updatedPageName = "other_name"
        val updatedSectionName = "other_section_name"
        val updatedContentBlockName = "other_content_block_name"

        startTestConnection {
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

    @Test
    fun `user updates`() = runTest {
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        var userDataModel: User? = null


        startTestConnection{
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
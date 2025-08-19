package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.common.events.ContextData
import po.exposify.common.events.DTOData
import po.exposify.dto.components.executioncontext.ContextEvents
import po.exposify.scope.launchers.update
import po.exposify.scope.service.models.TableCreateMode
import po.exposify.scope.sessions.withHooks
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.addHook
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.ContentBlockDTO
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockContentBlock
import po.test.exposify.setup.mocks.mockSection
import po.test.exposify.setup.mocks.mockedPage
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUpdate : DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<out CTX> = asIdentity()
    override val contextName: String = "TestUpdate"

    companion object {
        @JvmStatic
        var updatedById: Long = 0
    }

    @BeforeAll
    fun setup() = runTest {

        logHandler.notifierConfig {
            console = ConsoleBehaviour.MuteNoEvents
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
                updatedById = update(user).dataUnsafe.id
            }
        }
    }

    @Test
    fun `Saves new dto and verifies entire relation tree`() {
        val inputPage = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 2,
            contentBlocksCount = 2,
            updatedBy = updatedById
        ).first()
        inputPage.name = "TestPage"
        for (i in 0..inputPage.sections.size - 1) {
            val index = i + 1
            val section = inputPage.sections[i]
            section.name = "Section_$index"

            for (a in 0..section.contentBlocks.size - 1) {
                val indexA = a + 1
                val contentBlock = section.contentBlocks[a]
                contentBlock.name = "Content_${index}_$indexA"
            }
        }
        var updatedPage: Page? = null
        withConnection {
            service(PageDTO, TableCreateMode.Create) {
                updatedPage = update(inputPage).dataUnsafe
            }
        }

        val page = assertNotNull(updatedPage, "Updated page is null")
        val totalContBlocks = page.sections.sumOf { it.contentBlocks.size }
        assertAll(
            "Page properly updated",
            { assertNotEquals(0, page.id, "Id did not updated") },
            { assertEquals("TestPage", page.name, "Name property update failed") },
            { assertEquals(updatedById, page.updatedBy, "Name property update failed") },
            { assertEquals(4, totalContBlocks, "Sections count mismatch") }
        )

        val firstSection = page.sections.first()
        val lastSection = page.sections.last()

        assertNotSame(firstSection, lastSection, "First and last sections are the same object")

        assertAll(
            "First section properly updated",
            { assertNotEquals(0, firstSection.id, "Id did not updated") },
            { assertEquals("Section_1", firstSection.name, "Name property update failed") },
            { assertEquals(updatedById, firstSection.updatedBy, "Name property update failed") },
            { assertTrue(firstSection.contentBlocks.size == 2, "ContentBlocks count mismatch") }
        )

        assertAll(
            "Last section properly updated",
            { assertNotEquals(0, lastSection.id, "Id did not updated") },
            { assertEquals("Section_2", lastSection.name, "Name property update failed") },
            { assertEquals(updatedById, lastSection.updatedBy, "Name property update failed") },
            { assertTrue(lastSection.contentBlocks.size == 2, "ContentBlocks count mismatch") }
        )

        val firstContentBlock = firstSection.contentBlocks.first()
        val lastContentBlock = lastSection.contentBlocks.last()
        assertNotSame(firstContentBlock, lastContentBlock, "First and last content blocks are the same object")

        assertAll(
            "First ContentBlock properly updated",
            { assertNotEquals(0, firstContentBlock.id, "Id did not updated") },
            { assertEquals("Content_1_1", firstContentBlock.name, "Name property update failed") },
        )

        assertAll(
            "Last ContentBlock properly updated",
            { assertNotEquals(0, lastContentBlock.id, "Id did not updated") },
            { assertEquals("Content_2_2", lastContentBlock.name, "Name property update failed") },
        )
    }

}
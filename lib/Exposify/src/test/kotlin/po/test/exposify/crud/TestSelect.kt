package po.test.exposify.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.WhereQuery
import po.exposify.scope.service.enums.TableCreateMode
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.deferredWhere
import po.exposify.dto.components.result.ResultList
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.pagesSectionsContentBlocks
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSelect : DatabaseTest(), TasksManaged {


    override val contextName: String = "TestUpdate"

    companion object {
        @JvmStatic
        var updatedById: Long = 0
    }

    @BeforeAll
    fun setup() {

        val loggerHandler: LogNotifyHandler = logNotify()
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
    fun `Select retrieves persisted data properly`() {
        withConnection {
            val initialPage = pagesSectionsContentBlocks(
                pageCount = 1,
                sectionsCount = 2,
                contentBlocksCount = 2,
                updatedBy = updatedById
            ).first()
            for (i in 0..initialPage.sections.size - 1) {
                val index = i + 1
                val section = initialPage.sections[i]
                section.name = "Section_$index"

                for (a in 0..section.contentBlocks.size - 1) {
                    val indexA = a + 1
                    val contentBlock = section.contentBlocks[a]
                    contentBlock.name = "Content_${index}_$indexA"
                }
            }

            var persistedPages: List<Page> = emptyList()
            service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                update(initialPage).getDataForced()
                PageDTO.clearCachedDTOs()
                persistedPages = select().getData()
            }
            assertEquals(1, persistedPages.size, "Selected page count dos not match saved")

            val firstSection = persistedPages.first().sections.first()
            val lastSection = persistedPages.first().sections.last()

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
                {assertEquals(updatedById, lastSection.updatedBy, "UpdatedById property update failed") },
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

    @Test
    fun `ParentReference  property binding on select`() {
        val page = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 3,
            contentBlocksCount = 1,
            updatedBy = updatedById
        ).first()
        lateinit var selectedResult: ResultList<PageDTO, Page, PageEntity>
        withConnection {
            service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                update(page)
                selectedResult = select()
            }
        }

        val persistedPageDto = assertNotNull(selectedResult.getDTO().firstOrNull(), "Page(DTO) is null")
        val sectionDTOFirst = assertNotNull(persistedPageDto.sections.firstOrNull(), "Section(DTO) is null")
        val sectionDTOLast = assertNotNull(persistedPageDto.sections.lastOrNull(), "Section(DTO) is null")

        assertNotEquals(sectionDTOFirst, sectionDTOLast, "First and last Sections(DTO) are the same")

        assertAll(
            "page_id selected appropriately",
            { assertNotEquals(0, persistedPageDto.id, "id updated on select in PageDTO") },
            {
                assertEquals(
                    persistedPageDto.id,
                    sectionDTOFirst.page.id,
                    "PageId mismatch in first selected Section(DTO)"
                )
            },
            {
                assertEquals(
                    persistedPageDto.id,
                    sectionDTOLast.page.id,
                    "PageId mismatch in last selected Section(DTO)"
                )
            }
        )
    }

    @Test
    fun `Postgres serializable classes`() {
        val pages = pageModels(
            pageCount = 1,
            updatedBy = 1
        )

        val classList: List<ClassData> = listOf(ClassData(1, "str_1"), ClassData(2, "str2"))
        val inputPages =
            pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = updatedById, classes = classList)

        var persistedPages: List<Page> = emptyList()

        withConnection {
            service(PageDTO, TableCreateMode.FORCE_RECREATE) {
                update(inputPages).getData()
                PageDTO.clearCachedDTOs()
                persistedPages = select().getData()
            }
        }
        val inputSections = inputPages.flatMap { it.sections }
        val persistedSections = persistedPages.flatMap { it.sections }

        assertEquals(inputSections.size, persistedSections.size, "Sections count mismatch")
        val firstSection = persistedSections.first()
        val lastSection = persistedSections.last()
        assertNotSame(firstSection, lastSection, "First and last section is the same object")

        assertAll(
            "Serialized list on first section",
            { assertEquals(2, firstSection.classList.size, "Class list size mismatch") },
            { assertEquals(classList.first(), firstSection.classList.first(), "Persisted list does not match input") },
            { assertEquals(classList.last(), firstSection.classList.last(), "Persisted list does not match input") }
        )

        assertAll(
            "Serialized list on last section",
            { assertEquals(2, lastSection.classList.size, "Class list size mismatch") },
            { assertEquals(classList.first(), lastSection.classList.first(), "Persisted list does not match input") },
            { assertEquals(classList.last(), lastSection.classList.last(), "Persisted list does not match input") }
        )
    }

    @Test
    fun `Updates and selects with conditions`() {

        val pages = pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = updatedById)
        pages[0].langId = 1
        pages[1].langId = 2

        var selectedPages: List<Page> = emptyList()
        withConnection {
            service(PageDTO.Companion, TableCreateMode.FORCE_RECREATE) {
                update(pages)
                selectedPages = select(deferredWhere{ WhereQuery(Pages).equals(Pages.langId, 1)}).getData()
            }
        }

        assertEquals(1, selectedPages.count(), "Page count mismatch")
        val selectedSections = selectedPages[0].sections
        assertAll(
            { assertEquals(2, selectedSections.count(), "Selected Sections Mismatch") },
            { assertNotEquals(0, selectedSections[0].id, "Selected Section update failure") },
            { assertEquals(updatedById, selectedSections[0].updatedBy, "Selected Section updated with wrong updatedBy") }
        )
    }

    @Test
    fun `Property delegates update data correctly on update and select`(){
        val controlName = "Some Caption"
        val page = pagesSectionsContentBlocks(
            pageCount = 1,
            sectionsCount = 2,
            contentBlocksCount = 3,
            updatedBy = updatedById
        ).first()

        page.sections[0].name = controlName
        page.sections.forEach {section->
            section.name = controlName
            section.contentBlocks.forEach { contentBlock->
                contentBlock.content = controlName
            }
        }

        var updated : Page? = null
        var selected : Page? = null

        withConnection {
            service(PageDTO, TableCreateMode.FORCE_RECREATE){
                updated =  update(page).getData()
                selected = select().getData().firstOrNull()
            }
        }

        val updatedPage = assertNotNull(updated)
        val selectedPage = assertNotNull(selected)

        assertNotEquals(0, updatedPage.id)
        assertEquals(updatedPage.id, selectedPage.id, "Page id mismatch")
        assertTrue(updatedPage.sections.size == 2, "Sections empty in updated")
        assertTrue(selectedPage.sections.size == 2, "Sections empty in selected")
        assertEquals(controlName, updatedPage.sections.first().name, "Name property on Sections was not updated")
        assertEquals(controlName, selectedPage.sections.first().name, "Name property on Sections was not updated on Select")
        val updatedFirstSection = updatedPage.sections.first()
        val selectedFirstSection = selectedPage.sections.first()
        val selectedLastSection = selectedPage.sections.last()
        assertEquals(updatedFirstSection.name, updatedPage.sections.last().name, "Name property mismatch in firs and last updated Section")
        assertEquals(selectedFirstSection.name, selectedLastSection.name,"Name property mismatch in firs and last selected Section")
        assertTrue(updatedFirstSection.contentBlocks.size == 3, "ContentBlocks wrong size in updated")

        val firstContentBlockOfFistSection = selectedFirstSection.contentBlocks.first()
        val lastContentBlockOfLastSection = selectedLastSection.contentBlocks.last()
        assertAll("Asserting ContentBlocks of selection",
            { assertTrue(selectedFirstSection.contentBlocks.size == 3, "ContentBlocks empty in selected") },
            { assertEquals(controlName, firstContentBlockOfFistSection.content, "Content property on ContentBlock mismatch in selection") },
            { assertEquals(firstContentBlockOfFistSection.content, lastContentBlockOfLastSection.content, "Content property on ContentBlocks mismatch") }
        )
    }
}
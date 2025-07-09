package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertInstanceOf
import po.auth.extensions.generatePassword
import po.auth.extensions.withSessionContext
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.extensions.collectResult
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.sequence.extensions.switchContext
import po.exposify.scope.sequence.extensions.usingRoot
import po.exposify.scope.sequence.extensions.usingSwitch
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.NotifyConfig
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.Pages
import po.test.exposify.setup.SectionEntity
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModels
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequenceContext : DatabaseTest(), TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0

        @JvmStatic
        val session = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
    }



    @BeforeAll
    fun setup() = runTest {


        logHandler.notifierConfig {
            console = NotifyConfig.ConsoleBehaviour.MuteNoEvents
        }
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )
        withConnection {
            service(UserDTO.Companion, TableCreateMode.ForceRecreate) {
                updatedById = update(user).getDataForced().id
            }
        }
    }

    @Test
    fun `Running sequence as a DTO hierarchy child member`() = runTest {

        var pageDtoByOnResultCollected: PageDTO? = null
        fun testUpdated(result: ResultSingle<PageDTO, Page, PageEntity>) {
            pageDtoByOnResultCollected = result.getDTOForced()
            assertTrue(pageDtoByOnResultCollected.sections.size == 1, "Sections not updated")
        }

        withConnection {
            service(PageDTO, TableCreateMode.Create) {
                update(pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = updatedById))
                sequence(PageDTO.SELECT) {
                    switchContext(SectionDTO.UPDATE) { switchHandler ->
                        update(switchHandler.inputList)
                    }
                    select()
                }

                sequence(PageDTO.Companion.UPDATE) { handler ->
                    val insert = collectResult(update(handler.inputList))
                    switchContext(SectionDTO.SELECT_UPDATE) { switchHandler ->
                        update(switchHandler.inputList)
                    }
                    insert
                }
            }
        }

        var result: List<Section> = emptyList()
        assertDoesNotThrow {
            result = runSequence(SectionDTO.UPDATE, { PageDTO.switchQuery(1L) }) {
                withData(Section(1, "NewName", "NewDescription", "", emptyList(), emptyList(), 1, 1, 0))
            }.getData()
            assertTrue(result.count() == 1, "Section update did not succeed without explicit usingRoot")
        }

        val inputData = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = updatedById)
        val inputSectionUpdate = Section(1, "NewName", "NewDescription", "", emptyList(), emptyList(), 1, 1, 0)
        withSessionContext(session) {
            result = runSequence(SectionDTO.SELECT_UPDATE, { PageDTO.switchQuery(1L) }) {
                withData(inputSectionUpdate)
                usingRoot(PageDTO.UPDATE) {
                    withData(inputData)
                    onResultCollected(::testUpdated)
                }
            }.getData()
        }

        assertNotNull(pageDtoByOnResultCollected)
        assertTrue(result.count() == 1, "Section update did not return value")
        val section = result.first()

        assertAll(
            "Section update statement succeed",
            { assertInstanceOf<Section>(section, "Returned value is not of type Section") },
            { assertEquals("NewName", section.name, "Section properties update failed") },
            { assertNotEquals(0, section.id, "Updated section Section record") }
        )
    }

    @Test
    fun `Sequence launched with conditions and input work`() = runTest {
        var updatedPages: List<Page> = emptyList()
        withConnection {
            service(PageDTO.Companion, TableCreateMode.Create) {
                sequence(PageDTO.Companion.UPDATE) { handler ->
                    update(handler.inputList)
                }
                sequence(PageDTO.Companion.SELECT) { selectHandler ->
                    select(selectHandler.query)
                }
            }
        }

        val pages = pageModels(pageCount = 4, updatedBy = updatedById)
        pages[1].name = "this_name"
        pages[1].langId = 2
        pages[2].langId = 2
        pages[3].langId = 2

        withSessionContext(session) {
            updatedPages = runSequence(PageDTO.Companion.UPDATE) {
                withData(pages)
            }.getData()
        }



        assertAll(
            { assertEquals(4, updatedPages.count(), "Updated page count mismatch") },
            { assertNotEquals(0, updatedPages[0].id, "Page Update failure") },
            { assertEquals("this_name", updatedPages[1].name, "Updated page name mismatch") }
        )

        var selectPages: List<Page> = emptyList()
        withSessionContext(session) {
            selectPages = runSequence(PageDTO.Companion.SELECT) {
                withQuery { PageDTO.Companion.whereQuery().equals(Pages.langId, 2) }
            }.getData()
        }


        assertAll(
            { assertEquals(3, selectPages.count(), "Selected page count mismatch") },
            { assertNotEquals(0, selectPages[0].id, "Page Update failure") },
            { assertEquals("this_name", selectPages[0].name, "Selected page 1 name mismatch") }
        )
    }

    @Test
    fun `Running sequence as a DTO hierarchy root`() = runTest {

        var pageDtoByOnResultCollected: PageDTO? = null
        fun testUpdated(result: ResultSingle<PageDTO, Page, PageEntity>) {
            pageDtoByOnResultCollected = result.getDTOForced()
            assertTrue(pageDtoByOnResultCollected.sections.size == 1, "Sections not updated")
        }

        lateinit var sectionUpdateOutput: Section
        fun onSectionUpdated(result: ResultSingle<SectionDTO, Section, SectionEntity>) {
            sectionUpdateOutput = result.getDataForced()
        }
        withConnection {
            service(PageDTO.Companion, TableCreateMode.Create) {
                sequence(PageDTO.UPDATE) { handler ->
                    val insert = collectResult(update(handler.inputList))
                    switchContext(SectionDTO.UPDATE) { switchHandler ->
                        collectResult(update(switchHandler.inputList))
                    }
                    insert
                }
            }
        }

        val inputData = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = updatedById)
        val inputSectionUpdate = Section(1, "NewName", "NewDescription", "", emptyList(), emptyList(), 1, 1, 0)

        var result: List<Page> = emptyList()
        withSessionContext(session) {
            result = runSequence(PageDTO.Companion.UPDATE) {
                withData(inputData)
                onResultCollected(::testUpdated)
                usingSwitch(SectionDTO.Companion.UPDATE, { PageDTO.Companion.switchQuery(1L) }) {
                    withData(inputSectionUpdate)
                    onResultCollected(::onSectionUpdated)
                }
            }.getData()
        }

        assertTrue(result.count() == 1, "Page update did not return value")
        val page = result.first()

        assertAll(
            "Page update statement succeed",
            { assertInstanceOf<Page>(page, "Returned value is not of type Page") },
            { assertNotEquals(0, page.id, "Page id not assigned") }
        )
        assertAll(
            "Page update statement succeed",
            { assertInstanceOf<Section>(sectionUpdateOutput, "Returned value is not of type Page") },
            { assertEquals("NewName", sectionUpdateOutput.name, "Section properties update failed") },
            { assertEquals("NewDescription", sectionUpdateOutput.description, "Section properties update failed") },
            { assertNotEquals(0, sectionUpdateOutput.id, "Updated section Section record") }
        )
    }

}
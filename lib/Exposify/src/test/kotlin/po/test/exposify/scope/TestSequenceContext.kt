package po.test.exposify.scope

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import po.auth.extensions.generatePassword
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.toResultList
import po.exposify.scope.connection.ConnectionContext
import po.exposify.scope.sequence.extensions.runSequence
import po.exposify.scope.sequence.extensions.sequence
import po.exposify.scope.sequence.extensions.switchContext
import po.exposify.scope.sequence.extensions.usingConfig
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.logNotify
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.Pages
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
import kotlin.test.assertSame
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequenceContext : DatabaseTest(), TasksManaged {

    companion object{
        @JvmStatic
        var updatedById : Long = 0

        @JvmStatic
        lateinit var  connectionContext : ConnectionContext
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
        connectionContext = startTestConnection()
        connectionContext.service(UserDTO, TableCreateMode.FORCE_RECREATE) {
            updatedById = update(user).getDataForced().id
        }
    }

    @Test
    fun `Sequence switch statement select appropriate dto, update not breaking relations`() = runTest {

        var pageDtoByOnResultCollected: PageDTO? = null
        fun testUpdated(result : ResultSingle<PageDTO, Page, PageEntity>){
            pageDtoByOnResultCollected = result.getDTOForced()
            assertTrue(pageDtoByOnResultCollected.sections.size == 1, "Sections not updated")
        }

        connectionContext.run {
            service(PageDTO, TableCreateMode.CREATE){

                sequence(PageDTO.UPDATE){ handler->
                    val insert = handler.collectResult(update(handler.inputData))
                    pick(handler.query)
                    switchContext(SectionDTO.UPDATE){switchHandler->
                        update(switchHandler.inputList)
                    }
                    insert.toResultList()
                }

            }
        }

        val inputData = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = updatedById)
        val inputSectionUpdate = Section(1, "NewName", "NewDescription", "", emptyList(), emptyList(), 1, 1, 0)
        var updateResult : List<PageDTO>? = null

        val result =  runSequence(SectionDTO.UPDATE, {PageDTO.newQuery().byId(1L)} ){
            withData(inputSectionUpdate)
            updateResult = usingConfig(PageDTO.UPDATE){
                withData(inputData)
                onResultCollected(::testUpdated)
            }.getDTO()
        }.getData()

        val firstOfPageDtoInsert = assertNotNull(updateResult?.first())

        assertNotNull(pageDtoByOnResultCollected)
        assertSame(pageDtoByOnResultCollected, firstOfPageDtoInsert)
        assertTrue(result.count() == 1, "Section update did not return value")
        val section = result.first()

        assertAll("Section update statement succeed",
            { assertInstanceOf<Section>(section, "Returned value is not of type Section") },
            { assertEquals("NewName", section.name, "Section properties update failed") },
            { assertEquals(1, section.id, "Updated section Section record") }
        )
    }


    fun `Sequence launched with conditions and input work`() = runTest {
        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null")

        val pageClasses = listOf<ClassItem>(ClassItem(1, "class_1"), ClassItem(2, "class_2"))
        var updatedPages : List<Page> = emptyList()
        connectionContext.let { connection ->
            connection.service(PageDTO, TableCreateMode.CREATE) {
                sequence(PageDTO.UPDATE) { handler ->
                   handler.collectResult(update(handler.inputList))
                }
                sequence(PageDTO.SELECT) {selectHandler ->
                    select(selectHandler.query)
                }
            }
        }

        val pages = pageModels(pageCount = 4, updatedBy = updatedById)
        pages[1].name = "this_name"
        pages[1].langId = 2
        pages[2].langId = 2
        pages[3].langId = 2


        updatedPages = runSequence(PageDTO.UPDATE){
            withData(pages)
        }.getData()


        assertAll(
            { assertEquals(4, updatedPages.count(), "Updated page count mismatch") },
            { assertNotEquals(0, updatedPages[0].id, "Page Update failure") },
            { assertEquals("this_name", updatedPages[1].name, "Updated page name mismatch") }
        )
        val selectPages = runSequence(PageDTO.SELECT){
            withQuery(WhereQuery(Pages).equalsTo({langId}, 2))
        }.getData()

        assertAll(
            { assertEquals(3, selectPages.count(), "Selected page count mismatch") },
            { assertNotEquals(0, selectPages[0].id, "Page Update failure") },
            { assertEquals("this_name", selectPages[0].name, "Selected page 1 name mismatch") }
        )
    }
}
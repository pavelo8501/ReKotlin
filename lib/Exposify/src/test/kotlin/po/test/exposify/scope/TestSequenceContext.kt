package po.test.exposify.scope

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import po.auth.extensions.generatePassword
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.scope.connection.ConnectionContext
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.extensions.switch
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.LogNotifyHandler
import po.lognotify.TasksManaged
import po.lognotify.logNotify
import po.misc.exceptions.CoroutineInfo
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.MetaTag
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
            muteConsoleNoEvents = true
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

        var onSwitchSelection : String = ""
        var selectResult: List<Section>? = null

        connectionContext.let {connection->
            connection.service(PageDTO, TableCreateMode.CREATE) {

                sequence(SequenceID.UPDATE) {topHandler->
                    update(topHandler.inputData)
                    switch(SectionDTO){switchHandler->
                        update(switchHandler.inputData)
                    }
                }
            }
        }

        val sectionData = Section(0, "NewName", "NewDescription", "", emptyList<ClassItem>(), emptyList<MetaTag>(), 1, 1, 0)

        PageDTO.runSequence(PageDTO.createHandler(SequenceID.UPDATE)){
            withData(pageModelsWithSections(pageCount = 1, sectionsCount = 10, updatedBy = updatedById))
            withQuery(SectionDTO, SwitchQuery(PageDTO, 1L), sectionData)
        }



//
//
//        var sections : List<SectionDTO> = emptyList()
//
//         PageDTO.runSequence(SequenceID.SELECT){
//             sections = withResult{
//
//            }.getDTO() as List<SectionDTO>
//        }
//
//        val updatedSectionModel = sections.firstOrNull{it.id == 10L}
//
//        assertAll("End Session selection valid",
//            { assertEquals(10, sections.count(), "Received records count mismatch")  },
//            { assertNotNull(updatedSectionModel) },
//            { assertEquals("NewName", updatedSectionModel!!.name, "Updated model name mismatch") }
//        )
//
//        val deserializedSection  =  Json.decodeFromString<Section>(onSwitchSelection)
//        assertNotEquals(deserializedSection.name, updatedSectionModel!!.name, "Before update and after, same name")

    }


    @Test
    fun `sequence launched with conditions and input work`() = runTest {
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
                sequence(SequenceID.UPDATE) { handler ->
                   val result = update(handler.inputData)
                   handler.submitData(result)
                }
                sequence(SequenceID.SELECT) {handler ->
                    select(handler.inputQuery)
                }
            }
        }
        val pages = pageModels(pageCount = 4, updatedBy = updatedById)
        pages[1].name = "this_name"
        pages[1].langId = 2
        pages[2].langId = 2
        pages[3].langId = 2


        updatedPages = PageDTO.runSequence(PageDTO.createHandler(SequenceID.UPDATE)){
            println("Coroutine on init")
            println(CoroutineInfo.createInfo(kotlin.coroutines.coroutineContext))
            withData(pages)
        }

        assertAll(
            { assertEquals(4, updatedPages.count(), "Updated page count mismatch") },
            { assertNotEquals(0, updatedPages[0].id, "Page Update failure") },
            { assertEquals("this_name", updatedPages[1].name, "Updated page name mismatch") }
        )

        val selectPages =  PageDTO.runSequence(PageDTO.createHandler(SequenceID.SELECT)) {
            launchWithQuery(WhereQuery<Pages>().equalsTo(Pages.langId, 2))
        }

        assertAll(
            { assertEquals(3, selectPages.count(), "Selected page count mismatch") },
            { assertNotEquals(0, selectPages[0].id, "Page Update failure") },
            { assertEquals("this_name", selectPages[0].name, "Selected page 1 name mismatch") }
        )
    }
}
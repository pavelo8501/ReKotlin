package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import po.auth.extensions.generatePassword
import po.auth.extensions.session
import po.auth.sessions.models.AuthorizedSession
import po.exposify.common.events.ContextData
import po.exposify.common.events.DTOData
import po.exposify.dto.components.query.deferredQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.*
import po.exposify.scope.sequence.launcher.launch
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.Pages
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class TestSequenceCRUD : DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    override val contextName: String = "TestSequenceCRUD"

    private val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
    val session :  AuthorizedSession = session(sessionIdentity)

    companion object{
        @JvmStatic
        var updatedById : Long = 0
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
                updatedById = update(user).getDataForced().id
            }
        }
    }

    fun `Sequnenced SELECT execution`() = runTest {
        val pages: List<Page> = pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = 1)
        withConnection{
            service(PageDTO) {
                update(pages)
                sequenced(PageDTO.SELECT){handler->
                    select(handler) {

                    }
                }
            }
        }

        val selectResult: ResultList<PageDTO, Page, *> = assertDoesNotThrow {
            launch(PageDTO.SELECT, session)
        }
        assertNull(selectResult.failureCause, "Selection ended up with exception")
        assertEquals(2, selectResult.size, "Selection count mismatch")
    }


    fun `Sequnenced SELECT with query parameter`() = runTest {
        val pages: List<Page> = pageModelsWithSections(pageCount = 2, sectionsCount = 2, updatedBy = 1)
        withConnection{
            service(PageDTO) {
                update(pages)
                sequenced(PageDTO.SELECT){handler->
                    select(handler){

                    }
                }
            }
        }

        val selectResult: ResultList<PageDTO, Page, *> = assertDoesNotThrow {
            launch(PageDTO.SELECT, deferredQuery(PageDTO) { equals(Pages.name, "John") },  session)
        }
        assertNull(selectResult.failureCause, "Selection ended up with exception")
        assertEquals(2, selectResult.size, "Selection count mismatch")
    }


    fun `Sequnenced PICK BY ID execution`() = runTest {

        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = 1).first()
        var pickById = 0L

        withConnection {
            service(PageDTO) {
                pickById = update(page).getData()?.id?:0L
                sequenced(PageDTO.PICK) {handler->
                    pickById(handler){
                        withInputValue{
                            println("Input Value: $this")
                        }
                        withResult {
                            println("WithResult")
                            println(this)
                        }
                    }
                }
            }
        }
        with(session){
            val pickResult: ResultSingle<PageDTO, Page, *> = assertDoesNotThrow {
                launch(PageDTO.PICK, pickById)
            }
            assertNotEquals(0L, pickById, "pickID should not be 0")
            assertEquals(pickById, pickResult.getDTOForced().id, "Picked dto id does not match requested")
        }
    }

    @Test
    fun `Sequenced UPDATE statement`(){

        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = 1).first()
        withConnection {
            service(PageDTO) {
                select()
                sequenced(PageDTO.PICK) { handler ->
                    update(handler){

                    }
                }
            }
        }
    }


    fun `Simplified sequnence INSERT execution`() = runTest {

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = 1).first()
        withConnection {
            service(UserDTO) {
                update(user)
            }
//            service(PageDTO) {
//                sequenced(PageDTO.INSERT) { logHandler ->
//                    insert(logHandler.inputValue) {
//                        returnResult()
//                    }
//                }
//            }
//            launch(PageDTO.INSERT, page)
        }
    }
}
package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import po.auth.extensions.generatePassword
import po.auth.extensions.session
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.pickById
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.withInputValue
import po.exposify.scope.sequence.builder.withResult
import po.exposify.scope.sequence.launcher.launch
import po.lognotify.TasksManaged
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequence2 : DatabaseTest(), TasksManaged {

    val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")

    @Test
    fun `Sequnenced PICK BY ID execution`() = runTest {

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = 1).first()
        var pickById = 0L

        withConnection {
            service(UserDTO) {
                update(user)
            }
            service(PageDTO) {
                pickById = update(page).getData()?.id?:0L
                sequenced(PageDTO.PICK) {handler->
                    pickById(handler.input){
                        withInputValue {
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
        val session = session(sessionIdentity)
        with(session){
            val pickResult: ResultSingle<PageDTO, Page, *> = assertDoesNotThrow {
                launch(PageDTO.PICK, pickById)
            }
            assertNotEquals(0L, pickById, "pickID should not be 0")
            assertEquals(pickById, pickResult.getDTOForced().id, "Picked dto id does not match requested")
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
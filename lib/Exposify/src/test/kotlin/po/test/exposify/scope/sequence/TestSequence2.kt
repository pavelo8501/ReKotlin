package po.test.exposify.scope.sequence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.extensions.generatePassword
import po.exposify.scope.sequence.builder.insert
import po.exposify.scope.sequence.builder.pickById
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.launcher.launch
import po.lognotify.TasksManaged
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequence2 : DatabaseTest(), TasksManaged {
    @Test
    fun `Simplified sequnence PICK BY ID execution`() = runTest {

        val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
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
            service(PageDTO) {
                sequenced(PageDTO.PICK) {handler->
                    pickById(handler.inputValue){
                        returnResult()
                    }
                }
            }
            launch(PageDTO.PICK, 1L)
        }
    }


    @Test
    fun `Simplified sequnence INSERT execution`() = runTest {

        val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
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
            service(PageDTO) {
                sequenced(PageDTO.INSERT) { logHandler ->
                    insert(logHandler.inputValue) {
                        returnResult()
                    }
                }
            }
            launch(PageDTO.INSERT, page)
        }
    }
}
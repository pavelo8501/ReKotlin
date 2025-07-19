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
import po.exposify.scope.sequence.builder.switchStatement
import po.exposify.scope.sequence.builder.update
import po.exposify.scope.sequence.launcher.launchSwitch
import po.lognotify.TasksManaged
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.pageModelsWithSections

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSequenceSwitch : DatabaseTest(), TasksManaged {

    val sessionIdentity: TestSessionsContext.SessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")

    @Test
    fun `Sequnenced PICK BY ID with switch to child dto execution context`() = runTest {

        val user = User(
            id = 0,
            login = "some_login",
            hashedPassword = generatePassword("password"),
            name = "name",
            email = "nomail@void.null"
        )

        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = 1).first()
        var pickById = 0L

        val section: Section = pageModelsWithSections(pageCount = 1, sectionsCount = 1, updatedBy = 1).first().sections.first()

        withConnection {
            service(UserDTO) {
                update(user)
            }
            service(PageDTO) {
                pickById = update(page).getData()?.id ?: 0L

                sequenced(PageDTO.PICK) { handler ->
                    pickById(handler) {
                        switchStatement(SectionDTO.UPDATE){switchHandler->
                            update(switchHandler){

                            }
                        }
                    }
                }
            }
        }
        val session = session(sessionIdentity)

        val updatedSectionResult: ResultSingle<SectionDTO, Section, *> = assertDoesNotThrow {
            launchSwitch(SectionDTO.UPDATE, section, PageDTO.PICK, 1L, session)
        }

    }
}
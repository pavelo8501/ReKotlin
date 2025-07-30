package po.test.exposify.scope.sequence.crud

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.extensions.session
import po.auth.sessions.models.AuthorizedSession
import po.exposify.common.events.ContextData
import po.exposify.common.events.DTOData
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.update
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.scope.session.TestSessionsContext
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.pageModelsWithSections

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class TestUpdate: DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<out CTX> = asIdentity()
    private val sessionIdentity = TestSessionsContext.SessionIdentity("0", "192.169.1.1")
    val session :  AuthorizedSession = session(sessionIdentity)

    companion object{
        @JvmStatic
        var updatedById : Long = 0
    }
    @BeforeAll
    fun setup(){
        logHandler.notifierConfig {
            setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
            allowDebug(ContextData, DTOData)
        }
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }
    }

    @Test
    fun `Sequenced UPDATE statement`(){
        val page: Page = pageModelsWithSections(pageCount = 1, sectionsCount = 2, updatedBy = 1).first()
        withConnection {
            service(PageDTO) {
                insert(page)
                sequenced(PageDTO.PICK) { handler ->
                    update(handler){

                    }
                }
            }
        }
    }
}
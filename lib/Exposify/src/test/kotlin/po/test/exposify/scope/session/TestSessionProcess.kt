package po.test.exposify.scope.session

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.exposify.dto.components.ContextEvents
import po.exposify.dto.components.ContextListEvents
import po.exposify.scope.launchers.pick
import po.exposify.scope.launchers.select
import po.exposify.scope.launchers.update
import po.exposify.scope.sessions.withHooks
import po.exposify.scope.sessions.withListHooks
import po.lognotify.TasksManaged
import po.lognotify.launchers.runProcess
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.addHook
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.User
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedPage
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionProcess : DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestSessionProcess> = asIdentity()


    companion object {

        @JvmStatic
        var updatedBy: Long = 0

    }

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO) {
                val result = update(mockedUser)
                updatedBy = result.getDataForced().id
            }
        }
        withConnection {
            service(PageDTO){

            }
        }
    }

    @Test
    fun `Session data handled by process can be accessed inside contexts`() = runTest {

        val sectionsCount = 10
        var pickCallbackTriggered = false
        var selectCallbackSize: Int = 0

        val mockPages = mockPages(updatedBy, 2){
            withSections(sectionsCount){
                name = "section_$it"
                description = "description_$it"
            }
        }
        with(mockedSession){
            update(PageDTO, mockPages)
        }

        with(mockedSession){
            withHooks(PageDTO){
                addHook(ContextEvents.PickComplete, oneShot = false){dto->
                    pickCallbackTriggered = true
                }
            }
            withListHooks(SectionDTO){
                addHook(ContextListEvents.SelectComplete, oneShot = false){dtos->
                    selectCallbackSize = dtos.size
                }
            }
            val result = pick(PageDTO, 1L) {
                select(SectionDTO)
            }
            val sections = assertNotNull(result.dto)
            assertEquals(sectionsCount, sections.size)
            assertEquals(sectionsCount, selectCallbackSize)
            assertTrue(pickCallbackTriggered)
        }
    }
}
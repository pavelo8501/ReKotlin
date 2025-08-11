package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.ContextEvents
import po.exposify.dto.components.result.ResultSingle
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.inputs.withInput
import po.exposify.scope.sequence.launcher.launch
import po.exposify.scope.sequence.runtime.update
import po.exposify.scope.service.models.TableCreateMode
import po.exposify.scope.sessions.withHooks
import po.lognotify.TasksManaged
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.DebugData
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.debugging.DebugTopic
import po.misc.functions.registries.addHook
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPage
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockSections
import po.test.exposify.setup.mocks.mockedPage
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withSections
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.minutes

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUpdate : DatabaseTest(), TasksManaged {


    override val identity: CTXIdentity<TestUpdate> = asIdentity()

    companion object {
        @JvmStatic
        var updatedById: Long = 0

        @JvmStatic
        lateinit var page: Page
    }

    @BeforeAll
    fun setup() {
        logHandler.notifierConfig {
            setConsoleBehaviour(ConsoleBehaviour.MuteNoEvents)
        }
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }

        page = mockPage("Sequenced_UPDATE_mock", updatedById)
        withConnection {
            service(PageDTO) {
                insert(page)

                sequenced(PageDTO.Update) { handler ->
                    update(handler) {

                    }
                }
            }
        }
    }

    @Test
    fun `Sequenced UPDATE statement`(): TestResult = runTest {
        val updateValue = "DifferentName"
        val result = with(mockedSession) {
            val updatedPage = mockedPage
            updatedPage.name = updateValue
            launch(PageDTO.Update, updatedPage)
        }

        assertIs<ResultSingle<*, *>>(result)
        val updatedData = assertNotNull(result.data, "Result failure")
        assertEquals(updateValue, updatedData.name, "Page data was not updated")
        val pageDTO = assertNotNull(result.dto, "Result failure")
        assertEquals(updateValue, pageDTO.name)
    }

    @Test
    fun `Sequenced UPDATE statement with nested dtos`(): TestResult = runTest(timeout = 5.minutes) {
        val newPage = mockPages(updatedById, 1) {
            withSections(2){ index ->
                name = "Section_$index"
                description = "Section_Description_$index"
            }
        }.first()

        val result = with(mockedSession){

            withHooks(SectionDTO){
                addHook(ContextEvents.InsertComplete, oneShot = true){dto->
                    println(dto)
                }
                addHook(ContextEvents.UpdateComplete, oneShot = true){dto->
                    println(dto)
                }
            }

           val firstUpdateResult = launch(withInput(PageDTO.Update, newPage))
           page = assertNotNull(firstUpdateResult.data)

            page.sections.forEach {section->
                section.name = "updated_name_${section.id}"
                section.description = "updated_description_${section.id}"
            }
            launch(withInput(PageDTO.Update, page))
        }
        val updatedPage = assertNotNull(result.data)
        assertEquals(2, updatedPage.sections.size, "Section data models not assigned to Page data model")
        val lastSection = assertNotNull(updatedPage.sections.lastOrNull())
        assertNotNull(lastSection.id)
        assertEquals("updated_name_2", lastSection.name)
    }

}

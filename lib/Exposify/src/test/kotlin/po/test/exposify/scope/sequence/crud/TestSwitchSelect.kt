package po.test.exposify.scope.sequence.crud

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.exposify.dto.components.result.ResultList
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.switchDTO
import po.exposify.scope.sequence.inputs.withInput
import po.exposify.scope.sequence.inputs.withSwitchInput
import po.exposify.scope.sequence.launcher.launch

import po.exposify.scope.sequence.launcher.launchSwitching
import po.exposify.scope.sequence.runtime.pickById
import po.exposify.scope.sequence.runtime.select
import po.exposify.scope.sequence.runtime.update
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.scope.sequence.crud.TestSwitchUpdate.Companion.updatedById
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.dtos.UserDTO
import po.test.exposify.setup.mocks.mockPages
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import po.test.exposify.setup.mocks.withSections

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSwitchSelect: DatabaseTest(), TasksManaged  {

    override val identity: CTXIdentity<TestSwitchSelect> = asIdentity()

    @BeforeAll
    fun setup() {
        withConnection {
            service(UserDTO, TableCreateMode.ForceRecreate) {
                updatedById = update(mockedUser).getDataForced().id
            }
        }

        val pages: List<Page> = mockPages(updatedBy = updatedById, quantity = 2) {
            name = "page_$it"
            withSections(10) {
                name = "section_$it"
                description = "description_$it"
            }
        }

        withConnection {
            service(PageDTO) {
                update(pages)

                sequenced(PageDTO.Pick){handler->
                    pickById(handler){
                        switchDTO(SectionDTO.Select){handler->
                            select(handler){

                            }
                        }
                    }
                }
            }
        }
    }


    @Test
    fun `Sequenced PICK with switch SELECT statement`() = runTest{
        val pages: List<Page> = mockPages(updatedBy = updatedById, quantity = 2) {
            name = "page_$it"
            withSections(10) {
                name = "section_$it"
                description = "description_$it"
            }
        }


        val result : ResultList<SectionDTO, Section> = with(mockedSession){

            launch(withInput(PageDTO.Pick, 2L)){

                launchSwitching(withSwitchInput(SectionDTO.Select))

            }
        }
    }
}
package po.test.exposify.scope.sequence.builder

import org.junit.jupiter.api.Test
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SequenceDescriptor
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.sequenced
import po.exposify.scope.sequence.builder.switchStatement
import po.exposify.scope.sequence.inputs.DataInput
import po.exposify.scope.sequence.runtime.select
import po.exposify.scope.sequence.runtime.update
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.mocks.mockedSection
import po.test.exposify.setup.mocks.mockedSession
import po.test.exposify.setup.mocks.mockedUser
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class TestChunkContainers : DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestChunkContainers> = asIdentity()

    init {
        persistUser(mockedUser)
    }

    @Test
    fun `Chunk containers hierarchy properly created`() {
        withConnection {
            service(PageDTO) {
                sequenced(PageDTO.Select) { handler ->
                    select(handler) {
                        switchStatement(SectionDTO.Update) { handler ->
                            update(handler) {
                            }
                        }
                    }
                }
            }
        }
        val selectDescriptor = PageDTO.Select
        val topContainer = assertNotNull(selectDescriptor.containerBacking.value)
        val pageSelectContainer = assertIs<SequenceChunkContainer<PageDTO, Page>>(topContainer)
        val sectionUpdateDescriptor = SectionDTO.Update
        val subContainer = assertNotNull(sectionUpdateDescriptor.containerBacking.value)
        val sectionUpdateContainer = assertIs<SwitchChunkContainer<SectionDTO, Section, PageDTO, Page>>(subContainer)
        assertSame(pageSelectContainer, sectionUpdateContainer.parentContainer)
        assertEquals(1, subContainer.chunks.size)
    }

    @Test
    fun `Switch containers helper functions work as expected`() {

        withConnection {
            service(PageDTO) {
                sequenced(PageDTO.Select) { handler ->
                    select(handler) {
                        switchStatement(SectionDTO.Update) { handler ->
                            update(handler) {
                            }
                        }
                    }
                }
            }
        }
        val topContainer = assertNotNull(PageDTO.Select.containerBacking.value)

        lateinit var switchDataInput: DataInput<SectionDTO, Section>
        with(mockedSession) {
            switchDataInput =  DataInput(mockedSection, SectionDTO.Update)
        }
        var parentDescriptor = assertNotNull(topContainer.rootDescriptor)

        assertEquals(parentDescriptor as SequenceDescriptor<SectionDTO, Section>, switchDataInput.descriptor)

        assertIs<SequenceChunkContainer<PageDTO, Page>>(topContainer)


    }
}

package po.test.exposify.scope.sequence.builder

import org.junit.jupiter.api.Test
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.inputs.DataInput
import po.exposify.scope.sequence.inputs.ParameterInput
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.test.exposify.setup.DatabaseTest
import po.test.exposify.setup.dtos.Page
import po.test.exposify.setup.dtos.PageDTO
import po.test.exposify.setup.dtos.Section
import po.test.exposify.setup.dtos.SectionDTO
import po.test.exposify.setup.mocks.mockSection
import po.test.exposify.setup.mocks.mockedPage
import po.test.exposify.setup.mocks.mockedSection
import po.test.exposify.setup.mocks.mockedSession
import kotlin.test.assertTrue

class TestInputBase:DatabaseTest(), TasksManaged {

    override val identity: CTXIdentity<TestInputBase> = asIdentity()

    @Test
    fun `Input can be safely introspected`(){

        val rootUpdateContainer = SequenceChunkContainer(PageDTO.Update)
        val rootPickContainer = SequenceChunkContainer(PageDTO.Pick)

        lateinit var parameterInput: ParameterInput<SectionDTO, Section>
        lateinit var dataInput: DataInput<SectionDTO, Section>

        with(mockedSession){
            parameterInput =  ParameterInput(8L, SectionDTO.Pick)

            dataInput =  DataInput(mockedSection, SectionDTO.Update)
        }
        assertTrue((rootUpdateContainer.rootDescriptor == dataInput.descriptor) )
        assertTrue((rootPickContainer.rootDescriptor == parameterInput.descriptor) )

        assertTrue((rootUpdateContainer.rootDescriptor != parameterInput.descriptor) )
        assertTrue((rootPickContainer.rootDescriptor != dataInput.descriptor) )
    }


}
package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.ListResultMarker
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultMarker
import po.misc.containers.BackingContainer
import po.misc.containers.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity


sealed class SwitchDescriptorBase<DTO, D, E, F>(
    override val dtoClass: DTOClass<DTO, D, E>,
    val rootDescriptor:SequenceDescriptor<F, *, *>
): SequenceDescriptor<DTO, D, E>
        where DTO: ModelDTO, D: DataModel, E:LongEntity, F : ModelDTO
{
    override val chunksContainerBacking: BackingContainer<SequenceChunkContainer<DTO, D, E>> = backingContainerOf()

    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D, E>,
    ) = chunksContainerBacking.provideValue(sequenceContainer)

}

class SwitchSingeDescriptor<DTO, D, E, F>(
    dtoClass: DTOClass<DTO, D, E>,
    rootDescriptor:SequenceDescriptor<F, *, *>,
    val marker: SingleResultMarker
): SwitchDescriptorBase<DTO, D, E, F>(dtoClass, rootDescriptor) where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO{

    override val identity: CTXIdentity<SwitchSingeDescriptor<DTO, D, E, F>> = asIdentity()


}

class SwitchListDescriptor<DTO, D, E, F>(
    dtoClass: DTOClass<DTO, D, E>,
    rootDescriptor:SequenceDescriptor<F, *, *>,
    val marker: ListResultMarker
): SwitchDescriptorBase<DTO, D, E, F>(dtoClass, rootDescriptor) where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO{

    override val identity: CTXIdentity<SwitchListDescriptor<DTO, D, E, F>> = asIdentity()



}
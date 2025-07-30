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


sealed class SwitchDescriptorBase<DTO, D, F>(
    override val dtoClass: DTOClass<DTO, D, *>,
    val rootDescriptor:SequenceDescriptor<F, *, >
): SequenceDescriptor<DTO, D>
        where DTO: ModelDTO, D: DataModel, F : ModelDTO
{
    override val chunksContainerBacking: BackingContainer<SequenceChunkContainer<DTO, D>> = backingContainerOf()

    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D>,
    ) = chunksContainerBacking.provideValue(sequenceContainer)

}

class SwitchSingeDescriptor<DTO, D, F>(
    dtoClass: DTOClass<DTO, D, *>,
    rootDescriptor:SequenceDescriptor<F, *>,
    val marker: SingleResultMarker
): SwitchDescriptorBase<DTO, D, F>(dtoClass, rootDescriptor) where DTO: ModelDTO, D: DataModel,  F : ModelDTO{

    override val identity: CTXIdentity<SwitchSingeDescriptor<DTO, D,  F>> = asIdentity()

}

class SwitchListDescriptor<DTO, D, F>(
    dtoClass: DTOClass<DTO, D, *>,
    rootDescriptor:SequenceDescriptor<F, *>,
    val marker: ListResultMarker
): SwitchDescriptorBase<DTO, D, F>(dtoClass, rootDescriptor) where DTO: ModelDTO, D: DataModel, F : ModelDTO{

    override val identity: CTXIdentity<SwitchListDescriptor<DTO, D, F>> = asIdentity()



}
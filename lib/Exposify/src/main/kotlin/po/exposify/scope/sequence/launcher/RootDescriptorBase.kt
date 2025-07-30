package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.ListResultMarker
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultMarker
import po.misc.containers.BackingContainer
import po.misc.containers.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity


sealed class RootDescriptorBase<DTO, D>(
    override val  dtoClass: RootDTO<DTO, D, *>
): SequenceDescriptor<DTO, D>
        where DTO: ModelDTO, D: DataModel
{
    override val chunksContainerBacking: BackingContainer<SequenceChunkContainer<DTO, D>> = backingContainerOf()

    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D>
    ): Unit = chunksContainerBacking.provideValue(sequenceContainer)

}

class SingleDescriptor<DTO, D>(
    dtoClass: RootDTO<DTO, D, *>,
    val marker: SingleResultMarker
): RootDescriptorBase<DTO, D>(dtoClass) where DTO: ModelDTO, D: DataModel
{
    override val identity: CTXIdentity<SingleDescriptor<DTO, D>> = asIdentity()


}
class ListDescriptor<DTO, D>(
    dtoClass: RootDTO<DTO, D, *>,
): RootDescriptorBase<DTO, D>(dtoClass) where DTO: ModelDTO, D: DataModel
{
    override val identity: CTXIdentity<ListDescriptor<DTO, D>> = asIdentity()


}
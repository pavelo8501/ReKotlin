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


sealed class RootDescriptorBase<DTO, D, E>(
    override val  dtoClass: RootDTO<DTO, D, E>
): SequenceDescriptor<DTO, D, E>
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val chunksContainerBacking: BackingContainer<SequenceChunkContainer<DTO, D, E>> = backingContainerOf()

    fun  registerChunkContainer(
        sequenceContainer: SequenceChunkContainer<DTO, D, E>
    ): Unit = chunksContainerBacking.provideValue(sequenceContainer)

}

class SingleDescriptor<DTO, D, E>(
    dtoClass: RootDTO<DTO, D, E>,
    val marker: SingleResultMarker
): RootDescriptorBase<DTO, D, E>(dtoClass) where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val identity: CTXIdentity<SingleDescriptor<DTO, D, E>> = asIdentity()


}
class ListDescriptor<DTO, D, E>(
    dtoClass: RootDTO<DTO, D, E>,
): RootDescriptorBase<DTO, D, E>(dtoClass) where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val identity: CTXIdentity<ListDescriptor<DTO, D, E>> = asIdentity()


}
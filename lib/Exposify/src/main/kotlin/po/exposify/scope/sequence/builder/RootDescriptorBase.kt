package po.exposify.scope.sequence.builder

import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.containers.BackingContainer
import po.misc.containers.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import kotlin.text.appendLine


sealed class RootDescriptorBase<DTO, D>(
    override val dtoClass: RootDTO<DTO, D, *>,
) : SequenceDescriptor<DTO, D>
    where DTO : ModelDTO, D : DataModel {
    override val containerBacking: BackingContainer<ChunkContainer<DTO, D>> = backingContainerOf()

    fun registerChunkContainer(sequenceContainer: SequenceChunkContainer<DTO, D>): Unit =
        containerBacking.provideValue(sequenceContainer)
}

class SingleDescriptor<DTO, D>(
    dtoClass: RootDTO<DTO, D, *>,
) : RootDescriptorBase<DTO, D>(dtoClass) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<SingleDescriptor<DTO, D>> = asIdentity()

    override fun toString(): String =
        buildString {
            appendLine("SingleDescriptor[$dtoClass]")
            appendLine(containerBacking.value?.chunkCollectionSize ?: "null")
        }
}

class ListDescriptor<DTO, D>(
    dtoClass: RootDTO<DTO, D, *>,
) : RootDescriptorBase<DTO, D>(dtoClass) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<ListDescriptor<DTO, D>> = asIdentity()

    override fun toString(): String =
        buildString {
            appendLine("ListDescriptor[$dtoClass]")
            appendLine(containerBacking.value?.chunkCollectionSize ?: "null")
        }
}

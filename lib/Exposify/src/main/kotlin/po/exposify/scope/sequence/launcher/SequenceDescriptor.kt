package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.misc.containers.BackingContainer
import po.misc.context.CTX
import po.misc.types.TypeData

interface SequenceDescriptor<DTO: ModelDTO, D: DataModel, E: LongEntity> : CTX{
    val  dtoClass: DTOBase<DTO, D, E>
    val inputType: TypeData<D> get() = dtoClass.commonDTOType.dataType
    val chunksContainerBacking: BackingContainer<SequenceChunkContainer<DTO, D, E>>
}
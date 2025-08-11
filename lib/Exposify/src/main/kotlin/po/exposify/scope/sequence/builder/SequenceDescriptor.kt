package po.exposify.scope.sequence.builder

import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.containers.BackingContainer
import po.misc.context.CTX
import po.misc.types.TypeData

interface SequenceDescriptor<DTO : ModelDTO, D : DataModel> : CTX {
    val dtoClass: DTOBase<DTO, D, *>
    val inputType: TypeData<D> get() = dtoClass.commonDTOType.dataType
   // val containerBacking: BackingContainer<ChunkContainer<DTO, D>>

    fun getContainer():ChunkContainer<DTO, D>
}

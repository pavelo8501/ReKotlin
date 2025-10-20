package po.exposify.scope.sequence.builder

import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.context.CTX
import po.misc.types.token.TypeToken

interface SequenceDescriptor<DTO : ModelDTO, D : DataModel> : CTX {
    val dtoClass: DTOBase<DTO, D, *>
    val inputType: TypeToken<D> get() = dtoClass.commonDTOType.dataType
   // val containerBacking: BackingContainer<ChunkContainer<DTO, D>>

    fun getContainer():ChunkContainer<DTO, D>
}

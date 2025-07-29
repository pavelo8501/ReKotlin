package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations

fun <DTO, D, E> CommonDTO<DTO, D, E>.asDTO():DTO where DTO : ModelDTO, D : DataModel, E : LongEntity{
    return castOrOperations(commonType.dtoType.kClass, this)
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTO.asCommonDTO(
    dtoClass: DTOBase<DTO, D, E>
): CommonDTO<DTO, D, E> =  castOrOperations<CommonDTO<DTO, D, E>>(dtoClass)
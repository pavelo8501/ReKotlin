package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType

inline fun <reified DTO, reified D, reified E> dtoOf(
    sourceDTO: DTOBase<DTO, D, E>
): CommonDTOType<DTO, D, E> where DTO: ModelDTO, D: DataModel, E: LongEntity {

    val commonType = CommonDTOType.create(sourceDTO)
    return commonType
}


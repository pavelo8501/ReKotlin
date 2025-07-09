package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


@PublishedApi
internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.shallowDTO():CommonDTO<DTO, D, E> {
    val dto = config.dtoFactory.createDto()
    return dto
}

internal fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.newDTO(): CommonDTO<DTO,D,E>{
   val dto = config.dtoFactory.createDto()
    return  dto
}




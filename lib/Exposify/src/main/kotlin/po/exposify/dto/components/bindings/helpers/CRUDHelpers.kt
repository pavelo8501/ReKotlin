package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType


@PublishedApi
internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> RootDTO<DTO, D, E>.shallowDTO():CommonDTO<DTO, D, E> {
    val dto = dtoConfiguration.dtoFactory.createDto()
    return dto
}


internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel, FE: LongEntity> DTOClass<DTO, D, E>.shallowDTO(
    parentDTO: CommonDTO<F, FD, FE>
):CommonDTO<DTO, D, E> {

    return  parentDTO.withDTOContext(commonDTOType){
        dtoFactory.createDto()
    }
}

internal fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.newDTO(): CommonDTO<DTO,D,E>{
   val dto = dtoConfiguration.dtoFactory.createDto()
    return  dto
}

fun <DTO, D, E ,F, FD, FE, R> CommonDTO<DTO, D, E>.withDTOContext(
    commonDTOType: CommonDTOType<F, FD, FE>,
    block: DTOExecutionContext<F, FD, FE, DTO, D, E>.()-> R
):R where DTO: ModelDTO, D: DataModel, E: LongEntity ,F: ModelDTO, FD: DataModel, FE: LongEntity, R: Any {

    val context = executionContextMap.getUnsafeCasting<DTOExecutionContext<F, FD, FE, DTO, D, E>>(commonDTOType)
    return block.invoke(context)
}




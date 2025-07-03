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


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.shallowDTO():CommonDTO<DTO, D, E> {
    return config.dtoFactory.createDto()
}

fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.newDTO(
    data: D
):CommonDTO<DTO, D, E>{
    return config.dtoFactory.createDto(data)
}

fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.newDTO(
    dataList: List<D>
):List<CommonDTO<DTO, D, E>> = dataList.map { newDTO(it) }

fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.newDTO(
    entity:E
): CommonDTO<DTO,D,E>{
   val dto = config.dtoFactory.createDto()
    dto.provideEntity(entity)
    return  dto
}


internal fun  <DTO: ModelDTO, D: DataModel,  E: LongEntity> List<E>.select(
    dtoClass:DTOBase<DTO, D, E>,
    operation : CrudOperation
): ResultList<DTO, D, E>
{
    val result :  MutableList<CommonDTO<DTO, D, E>> = mutableListOf()
    forEach { entity ->
        val createdDTO =  dtoClass.config.dtoFactory.createDto()
        createdDTO.bindingHub.select(entity)
        result.add(createdDTO)
    }
    return  result.toResult(dtoClass, operation)
}


internal fun  <DTO: ModelDTO, D: DataModel,  E: LongEntity> E.select(
    dtoClass:DTOBase<DTO, D, E>,
    operation : CrudOperation
): ResultSingle<DTO, D, E>
{
    val createdDTO =  dtoClass.config.dtoFactory.createDto()
    createdDTO.bindingHub.select(this)
    return  createdDTO.toResult(operation)
}


/***
 * createByData Entry point for DTO hierarchy creation
 */
 internal fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> RootDTO<DTO, D, E>.createDTO(
    data:D,
    operation: CrudOperation
 ):CommonDTO<DTO, D, E>
 {
    val newDto = config.dtoFactory.createDto(data)
    newDto.bindingHub.create()
    return newDto
}

fun <DTO: ModelDTO, D: DataModel,  E: LongEntity> CommonDTO<DTO,D,E>.updateFromData(
    data: D,
    operation: CrudOperation
): ResultSingle<DTO, D, E> {
    bindingHub.update(data)
    return toResult(operation)
}





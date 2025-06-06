package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.shallowDTO():CommonDTO<DTO, D, E>
{
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


/***
 * createByData Entry point for DTO hierarchy creation
 */
 internal fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> RootDTO<DTO, D, E>.createDTO(
    data:D,
    operation: CrudOperation
 ):CommonDTO<DTO, D, E>
 {
    val newDto = config.dtoFactory.createDto(data)
    val insertedEntity = config.daoService.save { entity ->
        newDto.bindingHub.updateEntity(entity)
    }
    newDto.finalizeCreation(insertedEntity, Cardinality.ONE_TO_MANY)
    registerDTO(newDto)
    newDto.bindingHub.createChildByData()
    return newDto
}


//fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.createDTO(
//    data: D,
//    operation: CrudOperation
//):CommonDTO<DTO, D, E>{
//    val newDto = config.dtoFactory.createDto(data)
//    newDto.addTrackerInfo(operation, this)
//    return newDto.createByData()
//}

fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.createDTO(
    entities : List<E>,
    operation: CrudOperation,
): List<CommonDTO<DTO,D,E>>{
    val result :  MutableList<CommonDTO<DTO,D,E>> = mutableListOf()
    entities.forEach {entity->
        val created = createDTO(entity, operation)
        result.add(created)
    }
    return result.toList()
}


fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.createDTO(
    entity : E,
    operation: CrudOperation,
): CommonDTO<DTO,D,E>{
    val created =  newDTO(entity)

    created.addTrackerInfo(operation, this)
    created.bindingHub.createByEntity()
    created.finalizeCreation(entity, Cardinality.ONE_TO_MANY)
    return created
}


fun <DTO: ModelDTO, D: DataModel,  E: LongEntity> CommonDTO<DTO,D,E>.updateFromData(
    data: D,
    operation: CrudOperation
): ResultSingle<DTO, D, E> {
    addTrackerInfo(operation, dtoClass)
    bindingHub.updateFromData(data)
    return toResult(operation)
}





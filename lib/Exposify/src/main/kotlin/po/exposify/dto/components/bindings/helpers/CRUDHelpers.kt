package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO



fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.newDTO(
    data: D? = null,
):CommonDTO<DTO, D, E>{
    return config.dtoFactory.createDto(data)
}

fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.newDTO(
    dataList: List<D>
):List<CommonDTO<DTO, D, E>> = dataList.map { newDTO(it) }

fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  DTOBase<DTO, D, E>.newDTO(
    entity:E
): CommonDTO<DTO,D,E>{
    val created =  newDTO()
    created.provideInsertedEntity(entity)
    return  created
}


fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.createDTO(
    data: D,
    operation: CrudOperation
):CommonDTO<DTO, D, E>{
    val newDto = config.dtoFactory.createDto(data)
    newDto.addTrackerInfo(operation, this)
    return newDto.createByData()
}

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
    val created =  newDTO()
    created.addTrackerInfo(operation, this)
    created.provideInsertedEntity(entity)
    created.bindingHub.createByEntity()
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





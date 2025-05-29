package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO



fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.createDTO(
    data: D? = null
):CommonDTO<DTO, D, E>{
    val dto =  if(data != null){
        config.dtoFactory.createDto(data)
    }else{
        config.dtoFactory.createDto()
    }
    return dto
}

fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> createEntity(dto: CommonDTO<DTO, D, E>):E{
    val entity = dto.daoService.save(dto)
    dto.bindingHub.updateEntity(entity)
    return dto.provideInsertedEntity(entity)
}

fun  <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO, D, E>.createDTO(
    entity: E
):CommonDTO<DTO, D, E>{
    val dto = config.dtoFactory.createDto()
    dto.provideInsertedEntity(entity)
    return dto
}


fun <DTO: ModelDTO, D: DataModel,  E: LongEntity>  List<E>.createByEntity(
    dtoClass: DTOBase<DTO, D, E>,
    postProcess: ((CommonDTO<DTO,D,E>)-> Unit)? = null
): List<CommonDTO<DTO,D,E>>{
    val result :  MutableList<CommonDTO<DTO,D,E>> = mutableListOf()
    forEach {
        val created = dtoClass.createDTO(it).createFromEntity()
        postProcess?.invoke(created)
        result.add(created)
    }
    return result.toList()
}





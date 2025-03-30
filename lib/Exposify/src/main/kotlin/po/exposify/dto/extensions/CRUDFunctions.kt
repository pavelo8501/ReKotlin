package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binder.UpdateMode
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.models.CrudResult2
import po.exposify.dto.CommonDTO2
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.QueryConditions


internal suspend fun <DTO: ModelDTO, TB:  IdTable<Long>> runPick(dtoClass: DTOClass2<DTO>,  conditions: QueryConditions<TB>): CrudResult2<DTO>{
    val daoService = dtoClass.config.daoService
    val factory = dtoClass.config.dtoFactory

    val resultList = mutableListOf<CommonDTO2<DTO, *, *>>()
    val entity =  daoService.pick(conditions.build())
    entity?.let {entity->
        factory.createEntityDto()?.let { newDto->
            newDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            resultList.add(newDto)
        }
    }
    resultList.forEach {dto->   }
    return CrudResult2(resultList)
}

internal suspend inline fun <DTO: ModelDTO, TB : IdTable<Long>> DTOClass2<DTO>.pick(
    conditions: QueryConditions<TB>
): CrudResult2<DTO> = runPick(this, conditions)

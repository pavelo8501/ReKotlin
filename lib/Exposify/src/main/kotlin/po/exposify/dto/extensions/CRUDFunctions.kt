package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binder.UpdateMode
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.models.CrudResult2
import po.exposify.dto.CommonDTO2
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.QueryConditions
import kotlin.Long
import kotlin.collections.toList


internal suspend fun <DTO, DATA, ENTITY, TB: IdTable<Long>> runPick(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    conditions: QueryConditions<TB>): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    val resultList = mutableListOf<CommonDTO2<DTO, DATA, ENTITY>>()
    val entity =  config.daoService.pick(conditions.build())
    entity?.let {entity->

        config.dtoFactory.createEntityDto()?.let { newDto->
            newDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            resultList.add(newDto)
        }
    }
    resultList.forEach {  }
    return CrudResult2(resultList)
}


internal suspend fun <DTO, DATA, ENTITY, TB> runSelect(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    conditions: QueryConditions<TB>? = null
): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity, TB: IdTable<Long>{
    val resultList = mutableListOf<CommonDTO2<DTO, DATA, ENTITY>>()

    val entities = if(conditions!= null){
        config.daoService.select(conditions.build()).toList()
    }else{
        config.daoService.selectAll().toList()
    }

    entities.forEach {
        config.dtoFactory.createEntityDto()?.let {newDto->
            newDto.updateBinding(it, UpdateMode.ENTITY_TO_MODEL)
            resultList.add(newDto)
        }
    }
    return CrudResult2(resultList)
}


internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity, TB : IdTable<Long>> DTOClass2<DTO>.pick(
    conditions: QueryConditions<TB>
): CrudResult2<DTO> {
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runPick(this@pick, this@withTypedConfig, conditions)
    }
    return result
}

internal suspend inline fun <DTO, DATA, ENTITY, T>  DTOClass2<DTO>.select(
    conditions: QueryConditions<T>? = null
): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity, T: IdTable<Long>
{
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runSelect(this@select, this@withTypedConfig, conditions)
    }
    return result
}


//    internal suspend fun <T: IdTable<Long>> select(conditions: QueryConditions<T>): CrudResult<DATA, ENTITY> {
//        val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
//        val entities = daoService.select(conditions.build())
//        entities.forEach {
//            factory.createEntityDto()?.let {newDto->
//                newDto.updateBinding(it, UpdateMode.ENTITY_TO_MODEL)
//                resultList.add(newDto)
//            }
//        }
//        resultList.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories(it.entityDAO)
//        }
//        return CrudResult(resultList.toList())
//    }



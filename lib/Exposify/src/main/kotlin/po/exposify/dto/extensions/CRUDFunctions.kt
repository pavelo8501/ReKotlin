package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binder.UpdateMode
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.models.CrudResult2
import po.exposify.dto.CommonDTO2
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.extensions.update
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.QueryConditions
import kotlin.Long
import kotlin.collections.toList


internal suspend fun <DTO, DATA, ENTITY, TB: IdTable<Long>> runPick(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    conditions: QueryConditions<TB>): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity
{

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


internal suspend fun <DTO, DATA, ENTITY>runUpdate(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    dataModels: List<DATA>
): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {
    val resultList = mutableListOf<CommonDTO2<DTO, DATA, ENTITY>>()

    dataModels.forEach { dataModel ->
        config.dtoFactory.createEntityDto(dataModel)?.let { newDto ->
            config.relationBinder.createRepositories(newDto)
            resultList.add(newDto)
        }
    }
    return CrudResult2(resultList)
}


internal suspend fun <DTO, DATA, ENTITY>runDelete(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    dataModel: DATA): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity{

    val resultList = mutableListOf<CommonDTO2<DTO, DATA, ENTITY>>()
    config.dtoFactory.createEntityDto(dataModel)?.let { newDto ->
            config.daoService.selectWhere(newDto.id).let { entity ->
                newDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
                resultList.add(newDto)
            }
        }
    return CrudResult2(resultList)
}


/**
 * Selects a single entity from the database based on the provided conditions and maps it to a DTO.
 *
 * This function performs the following steps:
 * 1. Calls the `daoService.pick` method to retrieve a single entity that matches the given conditions.
 * 2. If an entity is found, a new DTO (`DTOFunctions<DATA, ENTITY>`) is created using the factory.
 * 3. The DTO is updated with the entity's data using `UpdateMode.ENTITY_TO_MODEL`.
 * 4. Relation bindings are applied to the DTO via `conf.relationBinder.applyBindings(it)`.
 * 5. Repository initialization is performed on the DTO via `it.initializeRepositories(it.entityDAO)`.
 * 6. Returns a `CrudResult` containing an list of DTO entities and any events recorded during the process.
 *
 * @param conditions A list of property-value pairs (`KProperty1<DATA, *>, Any?`)
 * representing the filtering conditions.
 * @return A `CrudResult<DATA, ENTITY>` containing the selected DTO (if found) and any triggered events.
 */
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity, TB : IdTable<Long>> DTOClass2<DTO>.pick(
    conditions: QueryConditions<TB>
): CrudResult2<DTO> {
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runPick(this@pick, this@withTypedConfig, conditions)
    }
    return result
}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA, ENTITY, T>  DTOClass2<DTO>.select(
    conditions: QueryConditions<T>
): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity, T: IdTable<Long>
{
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runSelect(this@select, this@withTypedConfig, conditions)
    }
    return result
}


internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass2<DTO>.select(

): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runSelect<DTO, DATA, ENTITY, IdTable<Long>>(this@select, this@withTypedConfig)
    }
    return result
}

internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass2<DTO>.update(
    dataModels: List<DATA>
): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity  {
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runUpdate(this@update, this@withTypedConfig, dataModels)
    }
    return result
}

/**
 * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
 *
 * @param dataModel The data model to delete.
 * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass2<DTO>.delete(
    dataModel: DATA
): CrudResult2<DTO>  where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{
    var result = CrudResult2(emptyList<CommonDTO2<DTO,DATA,ENTITY>>())
    withTypedConfig<DATA, ENTITY> {
        result = runDelete(this@delete, this@withTypedConfig, dataModel)
    }
    return result
}
package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binders.UpdateMode
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.models.CrudResult2
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.QueryConditions
import kotlin.Long
import kotlin.collections.toList


internal suspend fun <DTO, DATA, ENTITY, TB: IdTable<Long>> runPick(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    conditions: QueryConditions<TB>): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{
    val resultList = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
    val entity =  config.daoService.pick(conditions.build())
    entity?.let {entity->
        config.dtoFactory.createDto()?.let { newDto->
            newDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            resultList.add(newDto)
        }
    }
    return CrudResult2(resultList)
}


private suspend fun <DTO, TB> runSelect(
    dtoClass: DTOClass2<DTO>,
    conditions: QueryConditions<TB>? = null
): CrudResult2<DTO> where DTO : ModelDTO, TB: IdTable<Long>{

    val resultList = mutableListOf<CommonDTO<DTO, DataModel, ExposifyEntityBase>>()
    dtoClass.withDaoService {
        val entities = if(conditions!= null) {
            it.select(conditions.build()).toList()
        }else{
            it.selectAll().toList()
        }
        dtoClass.repository?.let { rootRepository ->
           val dtos =  rootRepository.select(entities)

        }
    }
    return CrudResult2(resultList)
}

private suspend fun <DTO>runUpdate(
    dtoClass: DTOClass2<DTO>,
    dataModels: List<DataModel>
): CrudResult2<DTO> where DTO : ModelDTO{
    val resultList = mutableListOf<CommonDTO<DTO, DataModel, ExposifyEntityBase>>()

    dtoClass.repository?.let {rootRepository->
        dataModels.forEach { dataModel ->
            dtoClass.config.dtoFactory.createDto(dataModel)?.let {
                resultList.add(it)
            }
        }
        rootRepository.update(resultList)
    }?: throw OperationsException("Root repository not initialized for ${dtoClass.personalName}", ExceptionCodes.KEY_NOT_FOUND)
    return CrudResult2(resultList)
}

internal suspend fun <DTO, DATA, ENTITY>runDelete(
    dtoClass: DTOClass2<DTO>,
    config :  DTOConfig2<DTO, DATA, ENTITY>,
    dataModel: DATA): CrudResult2<DTO> where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase{

    val resultList = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
    config.dtoFactory.createDto(dataModel)?.let { newDto ->
            config.daoService.selectById(newDto.id).let { entity ->
                if(entity != null){
                    newDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
                    resultList.add(newDto)
                }
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
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase, TB : IdTable<Long>> DTOClass2<DTO>.pick(
    conditions: QueryConditions<TB>
): CrudResult2<DTO> {
    var result = CrudResult2(emptyList<CommonDTO<DTO,DATA,ENTITY>>())
//    withTypedConfig<DATA, ENTITY> {
//        result = runPick(this@pick, this@withTypedConfig, conditions)
//    }
    return result
}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA, ENTITY, T>  DTOClass2<DTO>.select(
    conditions: QueryConditions<T>
): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase, T: IdTable<Long> = runSelect(this, conditions)


internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass2<DTO>.select(
): CrudResult2<DTO> where DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase = runSelect<DTO, IdTable<Long>>(this, null)


internal suspend fun <DTO> DTOClass2<DTO>.update(
    dataModels: List<DataModel>
): CrudResult2<DTO> where DTO: ModelDTO = runUpdate(this, dataModels)



/**
 * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
 *
 * @param dataModel The data model to delete.
 * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass2<DTO>.delete(
    dataModel: DATA
): CrudResult2<DTO>  where DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase
{
    var result = CrudResult2(emptyList<CommonDTO<DTO,DATA,ENTITY>>())
//    withTypedConfig<DATA, ENTITY> {
//        result = runDelete(this@delete, this@withTypedConfig, dataModel)
//    }
    return result
}
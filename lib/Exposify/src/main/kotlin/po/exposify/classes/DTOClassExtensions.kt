package po.exposify.classes

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.QueryConditions
import po.lognotify.extensions.getOrThrowCancellation
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.subTask
import po.lognotify.extensions.trueOrThrow
import kotlin.Long
import kotlin.collections.toList

private suspend fun <DTO, DATA, TB> runSelect(
    dtoClass: DTOClass<DTO>,
    conditions: QueryConditions<TB>? = null
): CrudResult<DTO, DATA> where DTO : ModelDTO, DATA: DataModel, TB: IdTable<Long>{

    val resultList = mutableListOf<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    dtoClass.withDaoService {
        val entities = if(conditions!= null) {
            it.select(conditions.build()).toList()
        }else{
            it.selectAll().toList()
        }
        dtoClass.repository?.let { rootRepository ->
           val dtos =  rootRepository.selectByByEntities(entities).filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
           resultList.addAll(dtos)
           println("Dtos count ${resultList.count()} fully initialized")
        }
    }
    return CrudResult(resultList)
}

private suspend fun <DTO>  runUpdate(
    dtoClass: DTOClass<DTO>,
    dataModels: List<DataModel>
): CrudResult<DTO, DataModel> where DTO : ModelDTO{

    val resultList = mutableListOf<CommonDTO<DTO, DataModel, ExposifyEntityBase>>()
    dtoClass.repository.getOrThrowDefault("Root repository undefined").updateByDataModels(dataModels).let {dtos->
        resultList.addAll(dtos)
        println("Dtos count ${resultList.count()} fully initialized")
    }

    return CrudResult(resultList)
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
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase, TB : IdTable<Long>> DTOClass<DTO>.pick(
    conditions: QueryConditions<TB>
): CrudResult<DTO, DATA> {
    val entity =  config.daoService.pick(conditions.build())
    val checked = entity.getOrThrowCancellation("Entity not wound")
    val dtos =  repository.getOrThrowCancellation("Repository uninitialized").selectByByEntities(listOf(checked))
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    return  CrudResult(checkedList)
}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA, ENTITY, T>  DTOClass<DTO>.select(
    conditions: QueryConditions<T>
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase, T: IdTable<Long> = runSelect(this, conditions)


internal suspend inline fun <DTO, DATA, ENTITY>  DTOClass<DTO>.select(
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase =
    subTask("Update Repository") {handler->
    isTransactionReady().trueOrThrow("Transaction should be active")
    val entities = config.daoService.selectAll().toList()
    val dtos = repository.getOrThrowCancellation("Repository uninitialized").selectByByEntities(entities)
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    handler.info("Created count ${checkedList.count()} DTOs")
    CrudResult<DTO, DATA>(checkedList)
}.resultOrException()


internal suspend fun <DTO, DATA> DTOClass<DTO>.updateRootDto(
    dataModels: List<DATA>,
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA : DataModel = subTask("Update Repository")  { handler->
    isTransactionReady().trueOrThrow("Transaction should be active")
    val dtos = repository.getOrThrowCancellation("Repository uninitialized").updateByDataModels(dataModels)
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    handler.info("Created ${checkedList.count()} DTOs")
    CrudResult<DTO, DATA>(checkedList)
}.resultOrException()



/**
 * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
 *
 * @param dataModel The data model to delete.
 * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA>  DTOClass<DTO>.delete(
    dataModel: DATA
): CrudResult<DTO, DATA>?  where DTO: ModelDTO, DATA: DataModel
{
    return null
}
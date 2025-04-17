package po.exposify.classes.extensions

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.CrudResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.WhereCondition
import po.lognotify.extensions.getOrThrowCancellation
import po.lognotify.extensions.subTask
import po.lognotify.extensions.trueOrThrow
import kotlin.Long
import kotlin.collections.toList


internal suspend fun <DATA> DTOClass<ModelDTO>.pickById(
    id: Long
): CrudResultSingle<ModelDTO, DATA>  where  DATA : DataModel{
    val freshDto = config.dtoFactory.createDto()
    val entity = freshDto.daoService.pickById(id)
    val checked = entity.getOrThrowCancellation("Entity not found")
    val dtos = repository.getOrThrowCancellation("Repository.kt uninitialized").select(listOf(checked))
    val checkedList = dtos.filterIsInstance<CommonDTO<ModelDTO, DATA, ExposifyEntityBase>>().toList()

   return CrudResultSingle(checkedList[0])
}


/**
 * Selects a single entity from the database based on the provided conditions and maps it to a DTO.
 *
 * This function performs the following steps:
 * 1. Calls the `daoService.pick` method to retrieve a single entity that matches the given conditions.
 * 2. If an entity is found, a new DTO (`DTOFunctions<DATA, ENTITY>`) is created using the factory.
 * 3. The DTO is updated with the entity's data using `UpdateMode.ENTITY_TO_MODEL`.
 * 4. Relation bindings are applied to the DTO via `conf.relationBinder.applyBindings(it)`.
 * 5. Repository.kt initialization is performed on the DTO via `it.initializeRepositories(it.entityDAO)`.
 * 6. Returns a `CrudResult` containing an list of DTO entities and any events recorded during the process.
 *
 * @param conditions A list of property-value pairs (`KProperty1<DATA, *>, Any?`)
 * representing the filtering conditions.
 * @return A `CrudResult<DATA, ENTITY>` containing the selected DTO (if found) and any triggered events.
 */
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase, TB : IdTable<Long>> DTOClass<DTO>.pick(
    conditions: WhereCondition<TB>
): CrudResult<DTO, DATA> {

    val freshDto = config.dtoFactory.createDto()
    val entity = freshDto.daoService.pick(conditions)
    val checked = entity.getOrThrowCancellation("Entity not found")
    val dtos =  repository.getOrThrowCancellation("Repository.kt uninitialized").select(listOf(checked))
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    return  CrudResult(checkedList)
}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
internal suspend fun <T, DTO, DATA> DTOClass<DTO>.select(
    conditions:  WhereCondition<T>
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel, T: IdTable<Long> =
    subTask("Select with conditions"){handler->

    isTransactionReady().trueOrThrow("Transaction should be active")
    val freshDto = config.dtoFactory.createDto()
    val entities = freshDto.daoService.select<T>(conditions)
    val dtos = repository.getOrThrowCancellation("Repository uninitialized in DTOClass").clear().select(entities)
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    handler.info("Created count ${checkedList.count()} DTOs")
    CrudResult<DTO, DATA>(checkedList)
}.resultOrException()


internal suspend fun <DTO, DATA> DTOClass<DTO>.select(
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel = subTask("Select") {handler->

    isTransactionReady().trueOrThrow("Transaction should be active")
    val freshDto = config.dtoFactory.createDto()
    val entities = freshDto.daoService.select().toList()
    val dtos = repository.getOrThrowCancellation("Repository.kt uninitialized in DTOClass").select(entities)
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    handler.info("Created count ${checkedList.count()} DTOs")
    CrudResult<DTO, DATA>(checkedList)
}.resultOrException()



internal suspend fun <DTO, DATA> DTOClass<DTO>.update(
    dataModel: DATA,
): CrudResultSingle<DTO, DATA> where DTO: ModelDTO, DATA : DataModel = subTask("Update Repository.kt")  { handler->
    isTransactionReady().trueOrThrow("Transaction should be active")
    val dtos = repository.getOrThrowCancellation("DTORepository uninitialized in DTOClass").update(listOf(dataModel))
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
    handler.info("Created ${checkedList.count()} DTOs")
    CrudResultSingle<DTO, DATA>(checkedList[0])
}.resultOrException()

internal suspend fun <DTO, DATA> DTOClass<DTO>.update(
    dataModels: List<DATA>,
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA : DataModel = subTask("Update Repository.kt")  { handler->
    isTransactionReady().trueOrThrow("Transaction should be active")
    val dtos = repository.getOrThrowCancellation("DTORepository uninitialized in DTOClass").update(dataModels)
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
internal suspend inline fun <DTO, DATA> DTOClass<DTO>.delete(
    dataModel: DATA
): CrudResult<DTO, DATA>?  where DTO: ModelDTO, DATA: DataModel
{
    return null
}
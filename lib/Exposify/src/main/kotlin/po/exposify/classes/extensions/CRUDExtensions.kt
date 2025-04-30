package po.exposify.classes.extensions

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.CrudResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.testOrThrow
import po.lognotify.extensions.subTask
import po.misc.types.castOrThrow
import kotlin.Long
import kotlin.collections.toList



internal suspend inline fun <DTO, DATA> DTOClass<DTO>.pickById(
    id: Long
): CrudResultSingle<DTO, DATA>  where DTO: ModelDTO,  DATA : DataModel{
    val freshDto = config.dtoFactory.createDto()
    val entity = freshDto.daoService.pickById(id)
    val checkedEntity = entity.getOrOperationsEx("Entity not found for id $id", ExceptionCode.VALUE_NOT_FOUND)
    val hierarchyRootRepository = repository.getOrOperationsEx("Repository uninitialized", ExceptionCode.REPOSITORY_NOT_FOUND)
    val dto =  hierarchyRootRepository.selectSingle(checkedEntity)
    val checkedDTO = dto.castOrThrow<CommonDTO<DTO, DATA, ExposifyEntity>, OperationsException>()
    return CrudResultSingle(checkedDTO)
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
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel,TB : IdTable<Long>> DTOClass<DTO>.pick(
    conditions: WhereCondition<TB>
): CrudResultSingle<DTO, DATA> {
    val freshDto = config.dtoFactory.createDto()
    val entity = freshDto.daoService.pick(conditions)
    val checkedEntity = entity.getOrOperationsEx("Entity not found", ExceptionCode.VALUE_NOT_FOUND)
    val hierarchyRootRepository = repository.getOrOperationsEx("Repository uninitialized", ExceptionCode.REPOSITORY_NOT_FOUND)
    val dto =  hierarchyRootRepository.selectSingle(checkedEntity)
    val checkedDTO = dto.castOrThrow<CommonDTO<DTO, DATA, ExposifyEntity>, OperationsException>()
    return CrudResultSingle(checkedDTO)
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

    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val freshDto = config.dtoFactory.createDto()
    val entities = freshDto.daoService.select<T>(conditions)
    val dtos = repository.getOrOperationsEx(
        "Repository uninitialized in DTOClass",
        ExceptionCode.REPOSITORY_NOT_FOUND).clear().select(entities)

    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntity>>()

    handler.info("Created count ${checkedList.count()} DTOs")
    CrudResult(checkedList)
}.resultOrException()


internal suspend fun <DTO, DATA> DTOClass<DTO>.select(
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel = subTask("Select") {handler->

    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val freshDto = config.dtoFactory.createDto()
    val entities = freshDto.daoService.select().toList()
    val dtos = repository.getOrOperationsEx("Repository uninitialized", ExceptionCode.REPOSITORY_NOT_FOUND).select(entities)
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntity>>()
    handler.info("Created count ${checkedList.count()} DTOs")
    CrudResult(checkedList)
}.resultOrException()



internal suspend fun <DTO, DATA> DTOClass<DTO>.update(
    dataModel: DATA,
): CrudResultSingle<DTO, DATA> where DTO: ModelDTO, DATA : DataModel = subTask("Update Repository.kt")  { handler->
    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val hierarchyRootRepository = repository.getOrOperationsEx("Repository uninitialized", ExceptionCode.REPOSITORY_NOT_FOUND)
    val dto = hierarchyRootRepository.updateSingle(dataModel)
    val checkedDto = dto.castOrThrow<CommonDTO<DTO, DATA, ExposifyEntity>, OperationsException>()
    handler.info("Created single DTO ${checkedDto.personalName}")
    CrudResultSingle(checkedDto)
}.resultOrException()

internal suspend fun <DTO, DATA> DTOClass<DTO>.update(
    dataModels: List<DATA>,
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA : DataModel = subTask("Update Repository.kt")  { handler->
    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val dtos = try{
        repository.getOrOperationsEx("Repository uninitialized", ExceptionCode.REPOSITORY_NOT_FOUND).update(dataModels)
    }catch (th: Throwable){
        println(th.message.toString())
        throw th
    }
    println("update point hit")
    val checkedList = dtos.filterIsInstance<CommonDTO<DTO, DATA, ExposifyEntity>>()
    handler.info("Created DTOs ${checkedList.count()}")
    CrudResult(checkedList)
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
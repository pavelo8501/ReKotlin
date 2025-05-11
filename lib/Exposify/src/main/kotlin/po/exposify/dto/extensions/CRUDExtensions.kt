package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.isTransactionReady
import po.exposify.extensions.testOrThrow
import po.lognotify.extensions.subTask
import kotlin.Long



//internal suspend inline fun <DTO, DATA, E> RootDTO<DTO, DATA, E>.pickById(
//    id: Long
//): ResultSingle<DTO, DATA, E>  where DTO: ModelDTO,  DATA : DataModel, E:LongEntity{
//    val entity =  config.daoService.pickById(id)
//    val checkedEntity = entity.getOrOperationsEx("Entity not found for id $id", ExceptionCode.VALUE_NOT_FOUND)
//    val executionProvider = RootExecutionProvider(this)
//    executionProvider.pick()p
//    val dto =   selectDto(this, checkedEntity)
//    return ResultSingle(dto)
//}


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
//internal suspend inline fun <DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity, TB : IdTable<Long>> RootDTO<DTO, DATA, ENTITY>.pick(
//    conditions: WhereQuery<TB>
//): ResultSingle<DTO, DATA, ENTITY> {
//    val entity = config.daoService.pick(conditions)
//    val checkedEntity = entity.getOrOperationsEx("Entity not found for conditions ${conditions.toString()}", ExceptionCode.VALUE_NOT_FOUND)
//    val dto = selectDto(this, checkedEntity)
//    return ResultSingle(dto)
//}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
//internal suspend fun <T, DTO, DATA, E> RootDTO<DTO, DATA, E>.select(
//    conditions:  WhereQuery<T>
//): ResultList<DTO, DATA, E> where DTO: ModelDTO, DATA: DataModel, E: LongEntity, T: IdTable<Long> =
//    subTask("Select with conditions"){handler->
//
//    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//        true
//    }
//    val entities = config.daoService.select(conditions)
//    val result = ResultList<DTO, DATA, E>()
//    entities.forEach {
//        val newDto = selectDto(this, it)
//        result.appendDto(newDto)
//    }
//    handler.info("Created count ${result.dtoList.count()} DTOs")
//    result
//}.resultOrException()

//
//internal suspend fun <DTO, DATA, E> RootDTO<DTO, DATA, E>.select()
//: ResultList<DTO, DATA, E> where DTO: ModelDTO, DATA: DataModel, E: LongEntity
//        = subTask("Select") {handler->
//    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//        true
//    }
//    val entities = config.daoService.select()
//    val result =  ResultList<DTO, DATA, E>()
//    entities.forEach {
//        val newDto = selectDto(this, it)
//        result.appendDto(newDto)
//    }
//    handler.info("Created count ${result.dtoList.count()} DTOs ")
//    result
//}.resultOrException()
//

//
//internal suspend fun <DTO, DATA, E> RootDTO<DTO, DATA, E>.update(
//    dataModel: DATA,
//): ResultSingle<DTO, DATA, E> where DTO: ModelDTO, DATA : DataModel, E : LongEntity
//        = subTask("Update Repository.kt")  { handler->
//    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//        true
//    }
//    val dto = updateDto<DTO, DATA, E>(this, dataModel)
//    handler.info("Created single DTO ${dto.dtoName}")
//    ResultSingle(dto)
//}.resultOrException()
//
//internal suspend fun <DTO, DATA, E> RootDTO<DTO, DATA, E>.update(
//    dataModels: List<DATA>,
//): ResultList<DTO, DATA, E> where DTO: ModelDTO, DATA : DataModel, E: LongEntity
//        = subTask("Update Repository.kt")  { handler->
//    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//        true
//    }
//    val result =  ResultList<DTO, DATA, E>()
//    dataModels.forEach {
//        val dto = updateDto<DTO, DATA, E>(this, it)
//        result.appendDto(dto)
//    }
//    handler.info("Created DTOs ${result.dtoList.count()}")
//    result
//}.resultOrException()
//

/**
 * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
 *
 * @param dataModel The data model to delete.
 * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
 */
//internal suspend inline fun <DTO, DATA, E> RootDTO<DTO, DATA, E>.delete(
//    dataModel: DATA
//): ResultList<DTO, DATA, E>?  where DTO: ModelDTO, DATA: DataModel, E: LongEntity
//{
//    return null
//}
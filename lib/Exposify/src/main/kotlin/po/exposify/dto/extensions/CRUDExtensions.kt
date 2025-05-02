package po.exposify.dto.extensions

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.CrudResultSingle
import po.exposify.dto.components.property_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.isTransactionReady
import po.exposify.extensions.testOrThrow
import po.lognotify.extensions.subTask
import po.misc.types.castOrThrow
import kotlin.Long
import kotlin.collections.toList


private suspend fun<DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity> selectDto(
     dtoClass: RootDTO<DTO, DATA>,
     entity: ENTITY
):CommonDTO<DTO, DATA, ENTITY>{

    val dto = dtoClass.config.dtoFactory.createDto()
    dtoClass.config.propertyBinder.update(dto.dataModel, entity, UpdateMode.ENTITY_TO_MODEL)
    dto.getDtoRepositories().forEach { it.select(entity) }
    return dto.castOrOperationsEx("selectDto. Cast failed.")
}

private suspend fun <DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity> updateDto(
    dtoClass: RootDTO<DTO, DATA>,
    dataModel: DATA
):CommonDTO<DTO, DATA, ENTITY>
{
    val dto = dtoClass.config.dtoFactory.createDto(dataModel)
    if(dataModel.id == 0L){
        dto.daoService.save(dto.castOrOperationsEx("updateDto(save). Cast failed."))
    }else{
        dto.daoService.update(dto.castOrOperationsEx("updateDto(update). Cast failed."))
    }
    dto.getDtoRepositories().forEach {repository->
        repository.update()
    }
    return dto.castOrOperationsEx("updateDto(Return). Cast failed.")
}


internal suspend inline fun <DTO, DATA, ENTITY> RootDTO<DTO, DATA>.pickById(
    id: Long
): CrudResultSingle<DTO, DATA>  where DTO: ModelDTO,  DATA : DataModel, ENTITY: ExposifyEntity{
    val entity =  config.daoService.pickById(id)
    val checkedEntity = entity.getOrOperationsEx("Entity not found for id $id", ExceptionCode.VALUE_NOT_FOUND)
    val dto = selectDto(this, checkedEntity)
    return CrudResultSingle(dto)
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
internal suspend inline fun <DTO: ModelDTO, DATA: DataModel,TB : IdTable<Long>> RootDTO<DTO, DATA>.pick(
    conditions: WhereCondition<TB>
): CrudResultSingle<DTO, DATA> {
    val entity = config.daoService.pick(conditions)
    val checkedEntity = entity.getOrOperationsEx("Entity not found for conditions ${conditions.toString()}", ExceptionCode.VALUE_NOT_FOUND)
    val dto = selectDto(this, checkedEntity)
    return CrudResultSingle(dto)
}

/**
 * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
 *
 * @return A [CrudResult] containing a list of initialized DTOs and associated events.
 */
internal suspend fun <T, DTO, DATA> RootDTO<DTO, DATA>.select(
    conditions:  WhereCondition<T>
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel, T: IdTable<Long> =
    subTask("Select with conditions"){handler->

    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val entities = config.daoService.select<T>(conditions)
    val result =  CrudResult(mutableListOf<CommonDTO<DTO, DATA, ExposifyEntity>>())
    entities.forEach {
        val newDto = selectDto(this, it)
        result.appendDto(newDto)
    }
    handler.info("Created count ${result.rootDTOs.count()} DTOs")
    result
}.resultOrException()


internal suspend fun <DTO, DATA> RootDTO<DTO, DATA>.select()
: CrudResult<DTO, DATA> where DTO: ModelDTO, DATA: DataModel
        = subTask("Select") {handler->
    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val entities = config.daoService.select()
    val result =  CrudResult(mutableListOf<CommonDTO<DTO, DATA, ExposifyEntity>>())
    entities.forEach {
        val newDto = selectDto(this, it)
        result.appendDto(newDto)
    }
    handler.info("Created count ${result.rootDTOs.count()} DTOs ")
    result
}.resultOrException()



internal suspend fun <DTO, DATA> RootDTO<DTO, DATA>.update(
    dataModel: DATA,
): CrudResultSingle<DTO, DATA> where DTO: ModelDTO, DATA : DataModel
        = subTask("Update Repository.kt")  { handler->
    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val dto = updateDto <DTO, DATA, ExposifyEntity>(this, dataModel)
    handler.info("Created single DTO ${dto.personalName}")
    CrudResultSingle(dto)
}.resultOrException()

internal suspend fun <DTO, DATA> RootDTO<DTO, DATA>.update(
    dataModels: List<DATA>,
): CrudResult<DTO, DATA> where DTO: ModelDTO, DATA : DataModel
        = subTask("Update Repository.kt")  { handler->
    isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
        true
    }
    val result =  CrudResult(mutableListOf<CommonDTO<DTO, DATA, ExposifyEntity>>())
    dataModels.forEach {
        val dto = updateDto<DTO, DATA, ExposifyEntity>(this, it)
        result.appendDto(dto)
    }
    handler.info("Created DTOs ${result.rootDTOs.count()}")
    result
}.resultOrException()


/**
 * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
 *
 * @param dataModel The data model to delete.
 * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
 */
internal suspend inline fun <DTO, DATA> RootDTO<DTO, DATA>.delete(
    dataModel: DATA
): CrudResult<DTO, DATA>?  where DTO: ModelDTO, DATA: DataModel
{
    return null
}
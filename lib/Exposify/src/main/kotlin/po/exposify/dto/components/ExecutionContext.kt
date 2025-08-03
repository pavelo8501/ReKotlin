package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.relation_binder.delegates.ParentDelegate
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.query.toSqlString
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.lognotify.TasksManaged
import po.lognotify.extensions.runTaskAsync
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.lambdaAsNotifier

sealed class ExecutionContext<DTO, DATA, ENTITY>(
    val dtoBase: DTOBase<DTO, DATA, ENTITY>,
) : TasksManaged where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {
    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoBase)

    private val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoBase.dtoConfiguration.daoService

    protected val insertWithNon0IdError: String = "insert statement should not be used for data with id other than 0"

    fun pickById(id: Long): ResultSingle<DTO, DATA> {
        val operation = CrudOperation.Pick
        return dtoBase.lookupDTO(id)?.toResult(operation) ?: run {
            val result = dtoBase.dtoConfiguration.daoService.pickById(id)
            if (result != null) {
                dtoBase
                    .newDTO()
                    .bindingHub
                    .loadHierarchyByEntity(result, dtoBase)
                    .toResult(operation)
            } else {
                val exception = operationsException("Entity with provided id :$id not found", ExceptionCode.DB_CRUD_FAILURE, this)
                dtoBase.toResult(exception)
            }
        }
    }

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA> {
        val operation = CrudOperation.Pick
        val entity = dtoBase.dtoConfiguration.daoService.pick(conditions)
        return if (entity != null) {
            dtoBase
                .newDTO()
                .bindingHub
                .loadHierarchyByEntity(entity, dtoBase)
                .toResult(operation)
        } else {
            val queryStr = conditions.build().toSqlString()
            val message = "Unable to find ${dtoBase.commonDTOType.dtoType} for query $queryStr"
            val exception =
                operationsException(
                    "Unable to find ${dtoBase.commonDTOType.dtoType} for query $queryStr",
                    ExceptionCode.DB_CRUD_FAILURE,
                    this,
                )
            dtoBase.toResult(exception)
        }
    }

    fun select(): ResultList<DTO, DATA> {
        val operation = CrudOperation.Select
        val entities = dtoBase.dtoConfiguration.daoService.select()
        return entities
            .map {
                val dto = dtoBase.newDTO()
                dto.bindingHub.loadHierarchyByEntity(it, dtoBase)
            }.toResult(dtoBase, operation)
    }

    fun select(conditions: SimpleQuery): ResultList<DTO, DATA> {
        val operation = CrudOperation.Select
        val entities = daoService.select(conditions)
        val dtos =
            entities.map {
                dtoBase.lookupDTO(it.id.value) ?: run {
                    val dto = dtoBase.newDTO()
                    dto.bindingHub.loadHierarchyByEntity(it, dtoBase)
                }
            }
        return dtos.toResult(dtoBase, operation)
    }

    fun select(conditions: WhereQuery<ENTITY>): ResultList<DTO, DATA> {
        val operation = CrudOperation.Select
        val entities = daoService.select(conditions)
        val dtos =
            entities.map {
                dtoBase.lookupDTO(it.id.value) ?: run {
                    val dto = dtoBase.newDTO()
                    dto.bindingHub.loadHierarchyByEntity(it, dtoBase)
                }
            }
        return dtos.toResult(dtoBase, operation)
    }

    fun delete(dataModel: DATA): ResultSingle<DTO, DATA> {
        TODO("Not yet implemented")
    }

    var onUpdateComplete: Notifier<CommonDTO<DTO, DATA, *>>? = null

    fun subscribeOnUpdateComplete(callback: (CommonDTO<DTO, DATA, *>) -> Unit) {
        onUpdateComplete = lambdaAsNotifier(callback)
    }
}

class RootExecutionContext<DTO, D, E>(
    val rootClass: RootDTO<DTO, D, E>,
) : ExecutionContext<DTO, D, E>(rootClass) where DTO : ModelDTO, D : DataModel, E : LongEntity {
    override val identity: CTXIdentity<RootExecutionContext<DTO, D, E>> = asSubIdentity(this, rootClass)

    var onInsertComplete: Notifier<CommonDTO<DTO, D, *>>? = null

    fun subscribeOnInsertComplete(callback: (CommonDTO<DTO, D, *>) -> Unit) {
        onInsertComplete = lambdaAsNotifier(callback)
    }

    fun insert(data: D): ResultSingle<DTO, D> {
        val operation = CrudOperation.Insert
        val dto = rootClass.newDTO().bindingHub.loadHierarchyByData(data, rootClass)
        onInsertComplete?.triggerUnsubscribing(dto)
        return dto.toResult(operation)
    }

    var onInsertListComplete: Notifier<List<CommonDTO<DTO, D, *>>>? = null

    fun subscribeOnInsertListComplete(callback: (List<CommonDTO<DTO, D, *>>) -> Unit) {
        onInsertListComplete = lambdaAsNotifier(callback)
    }

    fun insert(dataModels: List<D>): ResultList<DTO, D> {
        val result = dataModels.map { dataModel ->
                    dataModel.id = 0
                    insert(dataModel)
                }.toResult(rootClass)
        onInsertListComplete?.triggerUnsubscribing(result.getAsCommonDTO())
        return result
    }

    fun update(dataModel: D, initiator: CTX): ResultSingle<DTO, D> {
        val operation = CrudOperation.Update
        if (dataModel.id == 0L) {
            val result = insert(dataModel)
            onUpdateComplete?.triggerUnsubscribing(result.getAsCommonDTO())
            return result
        }
        val existentDto = rootClass.lookupDTO(dataModel.id)
        val result =
            if (existentDto != null) {
                existentDto.bindingHub.updatePropertiesBy(dataModel).toResult(operation)
            } else {
                val dto = rootClass.newDTO()
                dto.bindingHub.loadHierarchyByData(dataModel, initiator).toResult(operation)
            }
        onUpdateComplete?.triggerUnsubscribing(result.getAsCommonDTO())
        return result
    }

    var onUpdateListComplete: Notifier<List<CommonDTO<DTO, D, *>>>? = null
    fun subscribeOnUpdateListComplete(callback: (List<CommonDTO<DTO, D, *>>) -> Unit) {
        onUpdateListComplete = lambdaAsNotifier(callback)
    }
    fun update(dataModels: List<D>, initiator: CTX, ): ResultList<DTO, D> {
        val result = dataModels.map { update(it, initiator) }.toResult(rootClass)
        onUpdateListComplete?.triggerUnsubscribing(result.commonDTO)
        return result
    }
    override fun toString(): String = completeName
}

/**
 * Here dtoClass is a child class
 * hostingDTO parent Common DTO
 */
class DTOExecutionContext<DTO, D, E, F, FD, FE>(
    val dtoClass: DTOClass<DTO, D, E>,
    val hostingDTO: CommonDTO<F, FD, FE>,
    val dtoFactory: CommonDTOFactory<DTO, D, E, F, FD, FE> = CommonDTOFactory(hostingDTO, dtoClass.dtoConfiguration),
) : ExecutionContext<DTO, D, E>(dtoClass),
    TasksManaged
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    override val identity: CTXIdentity<DTOExecutionContext<DTO, D, E, F, FD, FE>> = asSubIdentity(this, hostingDTO)


    var onInsertComplete: Notifier<CommonDTO<DTO, D, *>>? = null
    fun subscribeOnInsertComplete(callback: (CommonDTO<DTO, D, *>) -> Unit) {
        onInsertComplete = lambdaAsNotifier(callback)
    }

    private fun lookupAndUpdateIfAny(dataModel: D): CommonDTO<DTO, D, E>? =
        dtoClass.lookupDTO(dataModel.id)?.bindingHub?.updatePropertiesBy(dataModel)

    private fun createByDataModel(data: D): CommonDTO<DTO, D, E>{
        val commonDTO = dtoFactory.createDto(data)

        if (commonDTO.dataStatus != DataStatus.PreflightCheckMock) {
            commonDTO.registerParentDTO(hostingDTO)
            val parentEntity = hostingDTO.entityContainer.getValue(this)

            commonDTO.bindingHub.resolveAttachedForeign(data)

            val delegate = commonDTO.bindingHub.getParentDelegateByType(hostingDTO.commonType)

            delegate.castOrOperations<ParentDelegate<DTO, D, E, F, FD, FE>>(this).let { castedDelegate ->
                val persistedEntity = commonDTO.daoService.save { ownEntity ->
                    commonDTO.bindingHub.updateEntityWithPropertyValues(ownEntity)
                    castedDelegate.entityProperty.set(ownEntity, parentEntity)
                }
                commonDTO.entityContainer.provideValue(persistedEntity)
            }
        } else {
            commonDTO.registerParentDTO(hostingDTO)
        }
        return commonDTO
    }

    fun insert(dataModel: D): ResultSingle<DTO, D> {
        val operation = CrudOperation.Insert
        if (dataModel.id != 0L) {
            throw operationsException(insertWithNon0IdError, ExceptionCode.METHOD_MISUSED)
        }
        val result = createByDataModel(dataModel).bindingHub.loadHierarchyByData(this).toResult(operation)
        onInsertComplete?.triggerUnsubscribing(result.getAsCommonDTO())
        return result
    }

    suspend fun update(dataModel: D, initiator: CTX): ResultSingle<DTO, D> =
        runTaskAsync("update") {
            val operation = CrudOperation.Update
            if (dataModel.id <= 0) {
                val commonDTO = createByDataModel(dataModel)
                commonDTO.bindingHub.loadHierarchyByData(this).toResult(operation)
            } else {
                lookupAndUpdateIfAny(dataModel)?.toResult(CrudOperation.Update) ?: run {
                    pickById(dataModel.id)
                }
            }
        }.resultOrException()


    var onUpdateListComplete: Notifier<List<CommonDTO<DTO, D, *>>>? = null
    fun subscribeOnUpdateListComplete(callback: (List<CommonDTO<DTO, D, *>>) -> Unit) {
        onUpdateListComplete = lambdaAsNotifier(callback)
    }
    suspend fun update(dataModels: List<D>, initiator: CTX): ResultList<DTO, D> = runTaskAsync("update") {
        val result = dataModels.map { update(it, initiator) }.toResult(dtoBase)
        onUpdateListComplete?.triggerUnsubscribing(result.commonDTO)
        result
        }.resultOrException()

    override fun toString(): String = completeName
}

internal fun <F, FD, FE, DTO, D, E> CommonDTO<DTO, D, E>.createDTOContext(
    dtoClass: DTOClass<F, FD, FE>,
    commonDTOType: CommonDTOType<F, FD, FE>,
): DTOExecutionContext<F, FD, FE, DTO, D, E>
where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    val newContext = DTOExecutionContext(dtoClass, this)
    registerExecutionContext(commonDTOType, newContext)
    return newContext
}

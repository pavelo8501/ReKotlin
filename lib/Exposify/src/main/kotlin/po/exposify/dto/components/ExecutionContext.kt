package po.exposify.dto.components

import kotlinx.coroutines.currentCoroutineContext
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.annotations.ExecutionContextDSL
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.withDTOContext
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.bindings.relation_binder.delegates.ParentDelegate
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.query.toSqlString
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ForeignDataModels
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.extensions.withSuspendedTransactionIfNone
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sessions.sessionInScope
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTask
import po.lognotify.launchers.runTaskAsync
import po.lognotify.notification.models.DebugData
import po.misc.collections.hasExactlyOne
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.containers.Notifier
import po.misc.functions.containers.lambdaAsNotifier
import po.misc.functions.registries.EmitterAwareRegistry
import po.misc.functions.registries.SubscriptionPack
import po.misc.functions.registries.buildRegistry
import po.misc.types.castListOrManaged
import po.misc.types.safeCast
import kotlin.coroutines.CoroutineContext


enum class ContextEvents{
    UpdateComplete,
    ListUpdateComplete,
    InsertComplete,
    ListInsertComplete
}

sealed class ExecutionContext<DTO, D, E>(
    private val dtoClass: DTOBase<DTO, D, E>,
) : TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    protected class SplitLists<D: DataModel>(
       private val dataList: List<D>
    ){
        val updateList : List<D> =  dataList.filter { it.id > 0L }
        val insertList: List<D> = dataList.filter { it.id <= 0L }
    }

    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

    protected val notifier: EmitterAwareRegistry<DTO, ContextEvents, CommonDTO<DTO, D, E>> = buildRegistry(dtoClass.commonDTOType.dtoType)
    protected val listNotifier: EmitterAwareRegistry<DTO, ContextEvents, List<CommonDTO<DTO, D, E>>> = buildRegistry(dtoClass.commonDTOType.dtoType)

    private val daoService: DAOService<DTO, D, E> get() = dtoClass.dtoConfiguration.daoService
    protected val dataModelTypeName: String = dtoClass.commonDTOType.dataType.typeName
    protected val insertHasNon0IdMsg: String = "Insert statement dataModels must not contain data with id other than 0"
    protected val wrongListSizeMsg: String = "Resulting list count must be exactly 1"
    protected val wrongRestoreListSizeMsg: String = "Upper DTOLookup list count must be exactly 1"

    protected val wrongDataTypeMsg:(String, String)-> String =
        {method, contextName-> "Wrong data type passed to $method update in $contextName" }

    protected val notFoundByQueryMsg: (String)-> String={queryStr->
        "${dtoClass.commonDTOType.dtoType} not found fro query $queryStr"
    }

    protected val abnormalState: ExceptionCode = ExceptionCode.ABNORMAL_STATE
    protected val methodMisuse: ExceptionCode =  ExceptionCode.METHOD_MISUSED

    protected fun restoreDTOWildcard(entities: List<*>):List<CommonDTO<DTO, D, E>> {

        entities.firstOrNull()?.let {
            if(it::class != dtoClass.commonDTOType.entityType.kClass){
                throw operationsException(wrongDataTypeMsg("restoreDTO", identifiedByName), methodMisuse)
            }
        }
        val casted = entities.castListOrManaged(dtoClass.commonDTOType.entityType.kClass, this)
        return restoreDTOS(casted)
    }

    protected fun restoreDTOS(entities: List<E>): List<CommonDTO<DTO, D, E>> {
        val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
        entities.forEach {entity->
            val commonDTO = dtoClass.newDTO()
            commonDTO.bindingHub.updateByEntity(entity)
            commonDTO.entityContainer.provideValue(entity)

            commonDTO.bindingHub.extractChildEntities(entity).forEach {container->
                if(container.entities.isNotEmpty()){
                    commonDTO.withDTOContextCreating(container.foreignDTO){
                        restoreDTOWildcard(container.entities)
                    }
                }
            }
            resultingList.add(commonDTO)
        }

        if(this is DTOExecutionContext<DTO, D, E, *, *, *>){
            hostingDTO.bindingHub.attachDTOS(resultingList, dtoClass.commonDTOType)
        }
        return resultingList
    }

//
//    fun select(conditions: SimpleQuery): ResultList<DTO, D> {
//        val operation = CrudOperation.Select
//        val entities = daoService.select(conditions)
//        val result = mutableListOf<CommonDTO<DTO, D, E>>()
//        if(dtoClass is RootDTO){
//                entities.forEach {
//                    dtoClass.lookupDTO(it.id.value) ?: run {
//                        val dto =  (dtoClass as RootDTO).newDTO()
//                        dto.bindingHub.loadHierarchyByEntity(it, dtoClass)
//                        result.add(dto)
//                    }
//                }
//        }else{
//            TODO("Branch not yet refactored")
//        }
//        return result.toResult(dtoClass, operation)
//    }
//
//    fun select(conditions: WhereQuery<E>): ResultList<DTO, D> {
//        val operation = CrudOperation.Select
//        val entities = daoService.select(conditions)
//        val result = mutableListOf<CommonDTO<DTO, D, E>>()
//        if(dtoClass is RootDTO){
//            entities.forEach {
//                dtoClass.lookupDTO(it.id.value) ?: run {
//                    val dto = (dtoClass as RootDTO).newDTO()
//                    dto.bindingHub.loadHierarchyByEntity(it, dtoClass)
//                    result.add(dto)
//                }
//            }
//        }else{
//            TODO("Branch not yet refactored")
//        }
//        return result.toResult(dtoClass, operation)
//    }

    fun delete(dataModel: D): ResultSingle<DTO, D> {
        TODO("Not yet implemented")
    }

   protected fun trySubscribingHooks(hooks: SubscriptionPack<CommonDTO<*, *, *>>?){
       if(hooks != null){
          notifier.trySubscribe(hooks)
       }
    }

    protected fun splitInputData(dataList: List<D>):SplitLists<D>{
       return SplitLists(dataList)
    }
}

class RootExecutionContext<DTO, D, E>(
    val dtoClass: RootDTO<DTO, D, E>,
) : ExecutionContext<DTO, D, E>(dtoClass) where DTO : ModelDTO, D : DataModel, E : LongEntity {
    override val identity: CTXIdentity<RootExecutionContext<DTO, D, E>> = asSubIdentity(this, dtoClass)

    private val daoService get() =  dtoClass.dtoConfiguration.daoService

    init {
        identity.setNamePattern { "RootExecutionContext[${dtoClass.identifiedByName}]" }
    }

    private fun tryGettingHooksFromContext(context: CoroutineContext?):SubscriptionPack<CommonDTO<DTO, D, E>>?{
      return context?.sessionInScope()?.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>("hooks")
    }

    private fun dtoLookup(entity: E): CommonDTO<DTO, D, E>{
        var existent = dtoClass.lookupDTO(entity.id.value)
        if(existent == null){
            existent  = restoreDTOS(listOf(entity)).hasExactlyOne {
                operationsException(wrongRestoreListSizeMsg, abnormalState)
            }
        }
        return existent
    }

    internal fun dtoLookup(id: Long): CommonDTO<DTO, D, E>?{
        var existent = dtoClass.lookupDTO(id)
        if(existent == null){
            dtoClass.dtoConfiguration.daoService.pickById(id)?.let {entity->
                val commonDTOList = restoreDTOS(listOf(entity))
                existent = commonDTOList.hasExactlyOne { operationsException(wrongRestoreListSizeMsg, abnormalState) }
            }
        }
        return existent
    }

    private fun runInsert(
        dataModels: List<D>,
        initialOperation:CrudOperation,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): List<CommonDTO<DTO, D, E>>{


        val result = mutableListOf<CommonDTO<DTO, D, E>>()

        dataModels.forEach {data->
            val commonDTO = dtoClass.newDTO(data)
            if (commonDTO.dataStatus != DataStatus.PreflightCheckMock) {
                val persistedEntity = commonDTO.daoService.save { entity ->
                    commonDTO.bindingHub.updateEntity(entity)
                    commonDTO.bindingHub.resolveAttachedForeign(data, entity)
                }
                persistedEntity.flush()
                commonDTO.entityContainer.provideValue(persistedEntity)

                val childDataList = commonDTO.bindingHub.extractChildData(data)
                childDataList.forEach {childData->
                    commonDTO.withDTOContextCreating(childData.foreignDTO){
                        insertWildcard(childData.dataModels, this, hooks?.safeCast())
                    }
                }
            }
            result.add(commonDTO)
        }
        return result
    }

    private fun runUpdate(
        dataModels: List<D>,
        initialOperation:CrudOperation,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): List<CommonDTO<DTO, D, E>>{
        val result = mutableListOf<CommonDTO<DTO, D, E>>()

        dataModels.forEach {data->
            val commonDTO = dtoLookup(data.id)
            if (commonDTO != null) {
                val childDataList = commonDTO.bindingHub.extractChildData(data)
                childDataList.forEach { childData ->
                    commonDTO.withDTOContextCreating(childData.foreignDTO){
                        updateWildcard(childData.dataModels, this, hooks?.safeCast())
                    }
                }
                result.add(commonDTO)
            }else {
                warning("DTO for data model $data not found")
            }
        }
        return result
    }

    suspend fun select(): ResultList<DTO, D> = runTaskAsync("select(All)"){
        val operation = CrudOperation.Select

        val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()

        daoService.select().forEach {entity->
           val commonDTO = dtoLookup(entity)
            resultingList.add(commonDTO)
        }
        dtoClass.toResult(resultingList)

//        val entities = dtoClass.dtoConfiguration.daoService.select()
//        if(dtoClass is RootDTO){
//            return entities
//                .map {
//                    val dto = dtoClass.newDTO()
//                    dto.bindingHub.loadHierarchyByEntity(it, dtoClass)
//                }.toResult(dtoClass, operation)
//        }else{
//            TODO("Branch not yet refactored")
//        }
    }.resultOrException()


    suspend fun select(conditions: SimpleQuery): ResultList<DTO, D> = runTaskAsync("select<SimpleQuery>"){

        val operation = CrudOperation.Select
        val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()


        daoService.select(conditions).forEach {entity->
            val commonDTO = dtoLookup(entity)
            resultingList.add(commonDTO)
        }
        dtoClass.toResult(resultingList)
//
//        val entities = daoService.select(conditions)
//        val result = mutableListOf<CommonDTO<DTO, D, E>>()
//        if(dtoClass is RootDTO){
//            entities.forEach {
//                dtoClass.lookupDTO(it.id.value) ?: run {
//                    val dto =  (dtoClass as RootDTO).newDTO()
//                    dto.bindingHub.loadHierarchyByEntity(it, dtoClass)
//                    result.add(dto)
//                }
//            }
//        }else{
//            TODO("Branch not yet refactored")
//        }
//        return result.toResult(dtoClass, operation)
    }.resultOrException()


    suspend fun pick(id: Long): ResultSingle<DTO, D> = runTaskAsync("Pick<$id>"){
        val operation = CrudOperation.Pick

        val session = currentCoroutineContext().sessionInScope()
        val hooks =  session?.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>("hooks")
        trySubscribingHooks(hooks?.safeCast())

         dtoClass.toResult(dtoLookup(id))

//        return dtoClass.lookupDTO(id)?.toResult(operation) ?: run {
//            val result = dtoClass.dtoConfiguration.daoService.pickById(id)
//            if (result != null) {
//                if(dtoClass is RootDTO){
//                    dtoClass
//                        .newDTO()
//                        .bindingHub
//                        .loadHierarchyByEntity(result, dtoClass)
//                        .toResult(operation)
//                }else{
//                    TODO("Branch not yet refactored")
//                }
//            } else {
//                val exception = operationsException("Entity with provided id :$id not found", ExceptionCode.DB_CRUD_FAILURE, this)
//                dtoClass.toResult(exception)
//            }
//        }
    }.resultOrException()

    suspend fun pick(conditions: SimpleQuery): ResultSingle<DTO, D> = runTaskAsync("Pick<SimpleQuery>"){
        val operation = CrudOperation.Pick
        daoService.pick(conditions)?.let {entity->
            val commonDTO = dtoLookup(entity.id.value)
            dtoClass.toResult(commonDTO)
        }?:run {
            val notFound = operationsException(notFoundByQueryMsg(conditions.build().toSqlString()), ExceptionCode.DTO_LOOKUP_FAILURE)
            dtoClass.toResult(notFound)
        }
//
//        val entity = dtoClass.dtoConfiguration.daoService.pick(conditions)
//        return if (entity != null ) {
//            if(dtoClass is RootDTO){
//                dtoClass
//                    .newDTO()
//                    .bindingHub
//                    .loadHierarchyByEntity(entity, dtoClass)
//                    .toResult(operation)
//            }else{
//                TODO("Branch not yet refactored")
//            }
//        } else {
//            val queryStr = conditions.build().toSqlString()
//            val message = "Unable to find ${dtoClass.commonDTOType.dtoType} for query $queryStr"
//            val exception =
//                operationsException(
//                    "Unable to find ${dtoClass.commonDTOType.dtoType} for query $queryStr",
//                    ExceptionCode.DB_CRUD_FAILURE,
//                    this,
//                )
//            dtoClass.toResult(exception)
//        }
    }.resultOrException()

    suspend fun insert(data: D): ResultSingle<DTO, D> = runTaskAsync("Insert<$dataModelTypeName>"){

        val hooks = currentCoroutineContext().sessionInScope()?.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>("hooks")
        trySubscribingHooks(hooks?.safeCast())

        withSuspendedTransactionIfNone(dtoClass.debugger, warnIfNoTransaction =  false) {
            val operation = CrudOperation.Insert
            if (data.id != 0L) {
                operationsException(insertHasNon0IdMsg, methodMisuse)
            }
            val commonDTO = runInsert(listOf(data), operation, hooks).hasExactlyOne {
                operationsException(wrongListSizeMsg, abnormalState)
            }

            notifier.trigger(ContextEvents.InsertComplete, commonDTO)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    suspend fun insert(dataModels: List<D>): ResultList<DTO, D> = runTaskAsync("Insert<List<$dataModelTypeName>>"){

        val hooks = currentCoroutineContext().sessionInScope()?.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>("hooks")
        trySubscribingHooks(hooks?.safeCast())
        withSuspendedTransactionIfNone(dtoClass.debugger, warnIfNoTransaction =  false) {
            val operation = CrudOperation.Insert
            val split = splitInputData(dataModels)
            if (split.updateList.isNotEmpty()) {
                operationsException(insertHasNon0IdMsg, ExceptionCode.METHOD_MISUSED)
            }
            val resultingList = runInsert(split.insertList, operation, hooks)
            listNotifier.trigger(ContextEvents.ListInsertComplete, resultingList)
            resultingList.toResult(dtoClass, operation)
        }
    }.resultOrException()

    suspend fun update(dataModel: D): ResultSingle<DTO, D> = runTaskAsync("Update<List<$dataModelTypeName>>"){

        val hooks = tryGettingHooksFromContext(currentCoroutineContext())
        trySubscribingHooks(hooks?.safeCast())

        withSuspendedTransactionIfNone(dtoClass.debugger, warnIfNoTransaction =  false) {
            val operation = CrudOperation.Update
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()

            if (dataModel.id == 0L) {
                val res = runInsert(listOf(dataModel), operation, hooks)
                resultingList.addAll(res)
            } else {
                val res = runUpdate(listOf(dataModel), operation, hooks)
                resultingList.addAll(res)
            }
            val commonDTO = resultingList.hasExactlyOne { operationsException(wrongListSizeMsg, ExceptionCode.ABNORMAL_STATE) }
            notifier.trigger(ContextEvents.UpdateComplete, commonDTO)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()


    suspend fun update(dataModels: List<D>): ResultList<DTO, D> = runTaskAsync("Update<List<$dataModelTypeName>>") {

        val hooks = tryGettingHooksFromContext(currentCoroutineContext())
        trySubscribingHooks(hooks?.safeCast())

        withSuspendedTransactionIfNone(dtoClass.debugger, warnIfNoTransaction =  false) {
            val operation = CrudOperation.Update
            val split = splitInputData(dataModels)

            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()

            if (split.insertList.isNotEmpty()) {
                val res = runInsert(split.insertList, operation, hooks)
                resultingList.addAll(res)
            }
            if (split.updateList.isNotEmpty()) {
                val res = runUpdate(split.updateList, operation, hooks)
                resultingList.addAll(res)
            }
            listNotifier.trigger(ContextEvents.ListUpdateComplete, resultingList)
            resultingList.toResult(dtoClass, operation)
        }
    }.resultOrException()
    override fun toString(): String = completeName
}


@ExecutionContextDSL
class DTOExecutionContext<DTO, D, E, F, FD, FE>(
    val dtoClass: DTOClass<DTO, D, E>,
    val hostingDTO: CommonDTO<F, FD, FE>
) : ExecutionContext<DTO, D, E>(dtoClass), TasksManaged
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity
{

    override val identity: CTXIdentity<DTOExecutionContext<DTO, D, E, F, FD, FE>> = asSubIdentity(this, hostingDTO)
    internal val dtoFactory: CommonDTOFactory<DTO, D, E, F, FD, FE> by lazy { CommonDTOFactory(hostingDTO, dtoClass.dtoConfiguration) }
    private  val daoService get() = dtoClass.dtoConfiguration.daoService
    private val commonDTOType get() = dtoClass.commonDTOType

    init {
        identity.setNamePattern { "DTOExecutionContext[${hostingDTO.identifiedByName}]" }
    }

    fun dtoLookup(entity: E): CommonDTO<DTO, D, E>{
        var existent =  hostingDTO.bindingHub.lookUpChild(entity.id.value, dtoClass.commonDTOType)
        if(existent == null){
            existent = restoreDTOS(listOf(entity)).hasExactlyOne {
                operationsException(wrongRestoreListSizeMsg, abnormalState)
            }
        }
        return existent
    }

    private fun dtoLookup(id: Long): CommonDTO<DTO, D, E>?{
        var existent =  hostingDTO.bindingHub.lookUpChild(id, dtoClass.commonDTOType)
        if(existent == null){
            dtoClass.dtoConfiguration.daoService.pickById(id)?.let {entity->
                val commonDTOList = restoreDTOS(listOf(entity))
                existent = commonDTOList.hasExactlyOne { operationsException(wrongRestoreListSizeMsg, abnormalState) }
            }
        }
        return existent
    }

    private fun runInsert(
        dataModels: List<D>,
        initialOperation:CrudOperation,
        initiator: CTX,
        hooks: SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): List<CommonDTO<DTO, D, E>>{
        val result = mutableListOf<CommonDTO<DTO, D, E>>()

        dataModels.forEach {data->
            val commonDTO = dtoFactory.createDto(data)
            if (commonDTO.dataStatus != DataStatus.PreflightCheckMock) {
                commonDTO.registerParentDTO(hostingDTO)
                val persistedEntity = commonDTO.daoService.save { entity ->
                    commonDTO.bindingHub.updateEntity(entity)
                    commonDTO.bindingHub.resolveAttachedForeign(data, entity)
                    val parentDelegate =  commonDTO.bindingHub.getParentDelegateByType(hostingDTO.commonType)
                        .getOrOperations(ParentDelegate::class, this)

                    parentDelegate.resolveParent(hostingDTO)
                    parentDelegate.bindEntity(entity, this)
                }
                persistedEntity.flush()
                commonDTO.entityContainer.provideValue(persistedEntity)
            }
            val childData =  commonDTO.bindingHub.extractChildData(data)
            childData.forEach { dataContainer ->
                val castedContainer = dataContainer.castOrOperations<ForeignDataModels<F, FD, FE>>(initiator)
                commonDTO.withDTOContextCreating(castedContainer.foreignDTO){
                    insertWildcard(castedContainer.dataModels, this, hooks?.safeCast())
                }
            }
            result.add(commonDTO)
        }
        return result
    }

    private fun runUpdate(
        dataModels: List<D>,
        initialOperation:CrudOperation,
        initiator: CTX,
        hooks: SubscriptionPack<CommonDTO<DTO, D, E>>?
    ):  List<CommonDTO<DTO, D, E>>{
        val result = mutableListOf<CommonDTO<DTO, D, E>>()
        dataModels.forEach {data->

            val commonDTO = dtoLookup(data.id)
            if (commonDTO != null) {
                val storedEntity = commonDTO.entityContainer.getValue(this)
                commonDTO.daoService.update(storedEntity){entity->
                    commonDTO.bindingHub.updateBy(data, entity)
                }
                val childDataList = commonDTO.bindingHub.extractChildData(data)
                childDataList.forEach { childData ->

                    commonDTO.withDTOContextCreating(childData.foreignDTO){
                        updateWildcard(childData.dataModels, initiator, hooks?.safeCast())
                    }
                }
                result.add(commonDTO)
            }else{
                warning("DTO for data model $data not found")
            }
        }
        return result
    }

    fun select(conditions: SimpleQuery): ResultList<DTO, D> = runTask("select<SimpleQuery>") {
        val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
        daoService.select(conditions).forEach {entity->
           val commonDTO = dtoLookup(entity)
            resultingList.add(commonDTO)
        }
        dtoClass.toResult(resultingList)
    }.resultOrException()

    fun select(): ResultList<DTO, D> = runTask("select(All)") {
        val operation = CrudOperation.Select
        val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
        hostingDTO.bindingHub.getRelationDelegateByType(commonDTOType)?.let {delegate->
            resultingList.addAll( delegate.childDTOS)
        }
        dtoClass.toResult(resultingList)

    }.resultOrException()

    fun pick(id: Long): ResultSingle<DTO, D> = runTask("Pick<$id>") {

       dtoClass.toResult(dtoLookup(id))
//        return dtoClass.lookupDTO(id)?.toResult(operation) ?: run {
//            val result = dtoClass.dtoConfiguration.daoService.pickById(id)
//            if (result != null) {
//                if(dtoClass is RootDTO){
//                    dtoClass
//                        .newDTO()
//                        .bindingHub
//                        .loadHierarchyByEntity(result, dtoClass)
//                        .toResult(operation)
//                }else{
//                    TODO("Branch not yet refactored")
//                }
//            } else {
//                val exception = operationsException("Entity with provided id :$id not found", ExceptionCode.DB_CRUD_FAILURE, this)
//                dtoClass.toResult(exception)
//            }
//        }
    }.resultOrException()


    fun pick(conditions: SimpleQuery): ResultSingle<DTO, D> = runTask("Pick<SimpleQuery>") {

        daoService.pick(conditions)?.let {entity->
            dtoClass.toResult(dtoLookup(entity))
        }?:run {
            val notFound = operationsException(notFoundByQueryMsg(conditions.build().toSqlString()), ExceptionCode.DTO_LOOKUP_FAILURE)
            dtoClass.toResult(notFound)
        }
//
//        val operation = CrudOperation.Pick
//        daoService.pick(conditions)?.let { entity ->
//            val commonDTO = dtoLookup(entity.id.value)
//            dtoClass.toResult(commonDTO)
//        } ?: run {
//            val notFound = operationsException(
//                notFoundByQueryMsg(conditions.build().toSqlString()),
//                ExceptionCode.DTO_LOOKUP_FAILURE
//            )
//            dtoClass.toResult(notFound)
//        }
    }.resultOrException()

    fun insert(
        dataModel: D,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): ResultSingle<DTO, D> = runTask("insert<$dataModelTypeName>") {

        trySubscribingHooks(hooks?.safeCast())

        val operation = CrudOperation.Insert
        if (dataModel.id != 0L) {
            throw operationsException(insertHasNon0IdMsg, methodMisuse)
        }
        val resultingList = runInsert(listOf(dataModel), operation,  initiator, hooks)
        val commonDTO =  resultingList.hasExactlyOne {  operationsException(wrongListSizeMsg, abnormalState) }

        notifier.trigger(ContextEvents.InsertComplete, commonDTO)
        dtoClass.toResult(commonDTO)
    }.resultOrException()


    fun insert(
        dataModels: List<D>,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): ResultList<DTO, D> = runTask("Insert<List<$dataModelTypeName>>") {

        trySubscribingHooks(hooks?.safeCast())

        val operation = CrudOperation.Insert
        val split = splitInputData(dataModels)

        if (split.updateList.isNotEmpty()) {
            operationsException(insertHasNon0IdMsg, methodMisuse)
        }
        val resultingList = runInsert(split.insertList, operation, initiator, hooks)
        listNotifier.trigger(ContextEvents.ListInsertComplete, resultingList)
        resultingList.toResult(dtoClass, operation)
    }.resultOrException()


    internal fun insertWildcard(
        dataModels: List<*>,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<*, *, *>>? = null
    ): ResultList<DTO, D>{
        dataModels.firstOrNull()?.let {
            if(it::class != dtoClass.commonDTOType.dataType.kClass){
                throw operationsException("Wrong data type passed to insert at $identifiedByName", abnormalState)
            }
        }
        val castedList = dataModels.castListOrManaged(dtoClass.commonDTOType.dataType.kClass, this)
        val castedHooks = hooks?.safeCast<SubscriptionPack<CommonDTO<DTO, D, E>>>()
        return insert(castedList, initiator, castedHooks)
    }

    fun update(
        dataModel: D,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): ResultSingle<DTO, D> = runTask("Update<$dataModelTypeName>") {
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {
            trySubscribingHooks(hooks?.safeCast())
            val operation = CrudOperation.Update
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            if (dataModel.id == 0L) {
                val res = runInsert(listOf(dataModel), operation, initiator, hooks)
                resultingList.addAll(res)
            } else {
                val res = runUpdate(listOf(dataModel), operation, initiator, hooks)
                resultingList.addAll(res)
            }
            val commonDTO = resultingList.hasExactlyOne { operationsException(wrongListSizeMsg, abnormalState) }
            notifier.trigger(ContextEvents.UpdateComplete, commonDTO)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()


    fun update(
        dataModels: List<D>,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<DTO, D, E>>?
    ): ResultList<DTO, D> = runTask("Update<List<${dataModelTypeName}>>"){

        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {

            trySubscribingHooks(hooks?.safeCast())
            val operation = CrudOperation.Update
            val split = splitInputData(dataModels)
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            if (split.insertList.isNotEmpty()) {
                val res = runInsert(split.insertList, operation, initiator, hooks)
                resultingList.addAll(res)
            }
            if (split.updateList.isNotEmpty()) {
                val res = runUpdate(split.updateList, operation, initiator, hooks)
                resultingList.addAll(res)
            }
            listNotifier.trigger(ContextEvents.ListUpdateComplete, resultingList)
            resultingList.toResult(dtoClass, operation)
        }

    }.resultOrException()

    internal fun updateWildcard(
        dataModels: List<*>,
        initiator: CTX,
        hooks:SubscriptionPack<CommonDTO<*, *, *>>?
    ): ResultList<DTO, D>{
        dataModels.firstOrNull()?.let {
            if(it::class != dtoClass.commonDTOType.dataType.kClass){
                throw operationsException(wrongDataTypeMsg("update", identifiedByName), methodMisuse)
            }
        }
        val castedList = dataModels.castListOrManaged(dtoClass.commonDTOType.dataType.kClass, this)
        val castedHooks = hooks?.safeCast<SubscriptionPack<CommonDTO<DTO, D, E>>>()
        return update(castedList, initiator, castedHooks)
    }
    override fun toString(): String = completeName
}

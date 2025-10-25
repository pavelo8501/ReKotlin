package po.exposify.dto.components.executioncontext

import kotlinx.coroutines.currentCoroutineContext
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dao.transaction.withTransactionIfNone
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.annotations.ExecutionContextDSL
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.bindings.relation_binder.delegates.withEachDelegate
import po.exposify.dto.components.bindings.withDTOHub
import po.exposify.dto.components.bindings.withHostDTOHub
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.toSqlString
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.dao.transaction.withSuspendedTransactionIfNone
import po.exposify.dto.components.result.ResultBase
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.configuration.TaskType
import po.lognotify.launchers.runTask
import po.lognotify.launchers.runTaskAsync
import po.lognotify.process.Process
import po.lognotify.process.activeProcess
import po.lognotify.process.process
import po.misc.collections.exactlyOneOrThrow
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asSubIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.functions.registries.SubscriptionPack
import po.misc.functions.registries.builders.taggedRegistryOf
import po.misc.types.castListOrManaged
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken

enum class ContextEvents{
    PickComplete,
    UpdateComplete,
    InsertComplete;

    companion object{
        override fun toString(): String {
           return "Hooks"
        }
    }
}

enum class ContextListEvents{
    SelectComplete,
    UpdateComplete,
    InsertComplete;

    companion object{
        override fun toString(): String {
            return "ListHooks"
        }
    }
}

internal class SplitLists<D: DataModel>(
    dataList: List<D>
){
    val updateList : List<D> =  dataList.filter { it.id > 0L }
    val insertList: List<D> = dataList.filter { it.id <= 0L }
}

sealed class ExecutionContext<DTO, D, E>(
    private val dtoClass: DTOBase<DTO, D, E>,
) : TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    abstract override val identity: CTXIdentity<out CTX>

    internal val notifier = taggedRegistryOf<ContextEvents, CommonDTO<DTO, D, E>>()
    internal val listNotifier = taggedRegistryOf<ContextListEvents, List<CommonDTO<DTO, D, E>>>()

    protected val daoService: DAOService<DTO, D, E> get() = dtoClass.dtoConfiguration.daoService
    protected val dtoFactory: DTOFactory<DTO, D, E> by lazy { DTOFactory(dtoClass.dtoConfiguration) }

    protected val  dataType: TypeToken<D> get() = dtoClass.commonDTOType.dataType

    internal val insertHasNon0IdMsg: String = "Insert statement dataModels must not contain data with id other than 0"
    internal val wrongListSizeMsg: String = "Resulting list count must be exactly 1"
    internal val wrongRestoreListSizeMsg: String = "Upper DTOLookup list count must be exactly 1"

    protected val wrongDataTypeMsg:(String, String)-> String =
        {method, contextName-> "Wrong data type passed to $method update in $contextName" }

    protected val notFoundByQueryMsg: (String)-> String={queryStr->
        "${dtoClass.commonDTOType.dtoType} not found fro query $queryStr"
    }

    internal val abnormalState: ExceptionCode = ExceptionCode.ABNORMAL_STATE
    internal val methodMisuse: ExceptionCode =  ExceptionCode.METHOD_MISUSED

    internal val dtoList: MutableList<CommonDTO<DTO, D, E>> = mutableListOf()

    init {
        notifier.identifiedByType(dtoClass.commonDTOType.dtoType)
        listNotifier.identifiedByType(dtoClass.commonDTOType.dtoType)
    }

    private val trackingList = mutableListOf<Pair<CTXIdentity<*>, (suspend (DTO)-> Unit)>>()
    internal fun setTracking(identity: CTXIdentity<*>, callback: suspend (DTO)-> Unit){
        trackingList.add(Pair(identity, callback))
    }

    private suspend fun findTracked(result:  ResultBase<DTO, D, *>){

        when(result){
            is ResultSingle->{
                result.data?.let {
                    if(it is Identifiable<*>){
                        val found = trackingList.firstOrNull { stored ->
                            stored.first.strictComparison(it.identity)
                        }
                        if (found != null) {
                            trackingList.remove(found)
                            found.second.invoke(result.getDTOForced())
                        }
                    }
                }
            }
            is ResultList->{
                result.data.forEach { data->
                    if(data is Identifiable<*>){

                        val found = trackingList.firstOrNull { stored ->
                            stored.first.strictComparison(data.identity)
                        }
                        if (found != null) {
                            trackingList.remove(found)
                            val dto =  result.dto.firstOrNull { it.id ==  data.id}.getOrManaged(this, ModelDTO::class)
                            found.second.invoke(dto)
                        }
                    }
                }
            }
        }
    }

    protected suspend fun afterUpdated(result:  ResultList<DTO, D>): ResultList<DTO, D>{
        if(trackingList.isNotEmpty()){
            findTracked(result)
        }
        return result
    }

    protected suspend fun afterUpdated(result:  ResultSingle<DTO, D>): ResultSingle<DTO, D>{
        if(trackingList.isNotEmpty()){
            findTracked(result)
        }
        return result
    }

    protected fun queryToNotFound(conditions: Op<Boolean>): OperationsException{
       return operationsException(notFoundByQueryMsg(conditions.toSqlString()), ExceptionCode.DTO_LOOKUP_FAILURE)
    }

    suspend fun delete(dataModel: D): ResultSingle<DTO, D>  = runTaskAsync("Delete(All)") {
        TODO("Not yet implemented")
    }.resultOrException()

    protected fun trySubscribingHooks(process: Process<AuthorizedSession>?) {
       process?.let {
           val hooks = it.receiver.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>(ContextEvents)
           if (hooks != null) {
               notifier.trySubscribe(hooks)
           }
       }
   }
    protected fun trySubscribingListHooks(process: Process<AuthorizedSession>?) {
        process?.let {
            val hooks = it.receiver.getExternalRef<SubscriptionPack<List<CommonDTO<DTO, D, E>>>>(ContextListEvents)
            if (hooks != null) {
               listNotifier.trySubscribe(hooks)
            }
        }
    }
    internal fun splitInputData(dataList: List<D>):SplitLists<D>{
       return SplitLists(dataList)
    }
}

class RootExecutionContext<DTO, D, E>(
    val dtoClass: RootDTO<DTO, D, E>,
) : ExecutionContext<DTO, D, E>(dtoClass) where DTO : ModelDTO, D : DataModel, E : LongEntity
{

    override val identity: CTXIdentity<RootExecutionContext<DTO, D, E>> = asSubIdentity(dtoClass)
    private val taskConfig = TaskConfig(taskType = TaskType.AsRootTask)

    init {
        identity.setNamePattern { "RootExecutionContext[${dtoClass.identifiedByName}]" }
    }

   suspend fun restoreDTO(dataModel:D): CommonDTO<DTO, D, E> {
       return insert(dataModel).getDTOForced().castOrOperations(this)
    }


   fun restoreDTO(entity:E): CommonDTO<DTO, D, E> {
       val dto = dtoClass.newDTO()
       withDTOHub(dto){
           resolveAttachedForeign(this, entity)
           updateByEntity(entity)
           relationDelegateMap.values.forEach {delegate->
               hostingDTO.withDTOContextCreating(delegate.dtoClass){
                   restoreDTO(this, entity)
               }
           }
       }
       dtoList.add(dto)
       return dto
    }

    private  fun dtoLookup(entity: E): CommonDTO<DTO, D, E> {
        var existent =  dtoList.firstOrNull { it.id ==  entity.id.value}
        if (existent == null) {
            existent = restoreDTO(entity)
        }
        return existent.getOrOperations(this)
    }

    private fun runInsert(dataModel: D): CommonDTO<DTO, D, E> {
        val commonDTO = dtoClass.newDTO(dataModel)
        if (commonDTO.dataStatus == DataStatus.PreflightCheckMock) {
            return commonDTO
        }
        withDTOHub(commonDTO) { dto ->
            resolveAttachedForeign(this, dataModel)
            val persistedEntity = dto.daoService.save { entity ->
                updateEntity(entity)
            }
            persistedEntity.flush()
            dto.entityContainer.provideValue(persistedEntity)

            relationDelegateMap.values.forEach { childDelegate ->
                dto.withDTOContextCreating(childDelegate.dtoClass) {
                    insert(this)
                }
            }
        }
        dtoList.add(commonDTO)
        return commonDTO
    }

    private suspend fun runUpdate(
        dataModel:D
    ): CommonDTO<DTO, D, E> {
        val commonDTO = dtoList.firstOrNull { it.id ==  dataModel.id} ?: run { restoreDTO(dataModel) }
        withDTOHub(commonDTO) {
            val dtoEntity = commonDTO.entityContainer.getValue(this)
            val persistedEntity = daoService.update(dtoEntity) { entity ->
                updateEntity(entity)
            }
            persistedEntity.flush()
            relationDelegateMap.values.forEach { relationDelegate ->
                hostingDTO.withDTOContextCreating(relationDelegate.dtoClass) {
                    update(this)
                }
            }
        }
        return commonDTO
    }

    suspend fun select(): ResultList<DTO, D> = runTaskAsync("select(All)", taskConfig) {
        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            val entities = daoService.select()
            entities.forEach { entity ->
                val existent =   dtoList.firstOrNull { it.id == entity.id.value }
                if(existent != null){
                    "Selected existent".output(Colour.Magenta)
                    existent.output(Colour.Magenta)
                    resultingList.add(existent)
                }else{
                    "Initiating restore process for entity type: {${daoService.entityType}}  id: ${entity.id.value}".output(Colour.Magenta)
                   val commonDTO = dtoClass.newDTO()
                    withDTOHub(commonDTO){
                        resolveAttachedForeign(this, entity)
                        updateByEntity(entity)
                    }
                    commonDTO.entityContainer.provideValue(entity)
                    commonDTO.bindingHub.relationDelegateMap.values.forEach { delegate->
                       commonDTO.withDTOContextCreating(delegate.dtoClass){
                          restoreDTO(this, entity)
                       }
                    }
                    commonDTO.bindingHub.applyDataModels()
                    resultingList.add(commonDTO)
                    dtoList.add(commonDTO)
                }
            }
            dtoClass.toResult(resultingList)
        }
    }.resultOrException()

    suspend fun select(conditions: SimpleQuery): ResultList<DTO, D> =
        runTaskAsync("select<$${dataType.typeName}>(SimpleQuery)", taskConfig) {

        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            daoService.select(conditions).forEach { entity ->
                val commonDTO = dtoLookup(entity)
                resultingList.add(commonDTO)
            }
            dtoClass.toResult(resultingList)
        }
    }.resultOrException()

    suspend fun pick(id: Long): ResultSingle<DTO, D> =
        runTaskAsync("Pick<$${dataType.typeName}>($id)", taskConfig) {

        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentCoroutineContext()) {
            val commonDTO = dtoList.firstOrNull { it.id ==  id} ?:run {
                 daoService.pickById(id)?.let {  restoreDTO(it)  }
            }
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    suspend fun pick(conditions: SimpleQuery): ResultSingle<DTO, D> =
        runTaskAsync("Pick<${dataType.typeName}>(SimpleQuery)", taskConfig) {

        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {

            val entity = daoService.pick(conditions)
            if (entity != null) {
                val commonDTO = dtoList.firstOrNull { it.id ==  entity.id.value}?: run {
                    restoreDTO(entity)
                }
                dtoClass.toResult(commonDTO)
            } else {
                dtoClass.toResult(queryToNotFound(conditions.build()))
            }
        }
    }.resultOrException()

    suspend fun insert(data: D): ResultSingle<DTO, D> =
        runTaskAsync("Insert<${dataType.typeName}>(id=${data.id})", taskConfig) {

        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process<AuthorizedSession>())
       val result = withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val operation = CrudOperation.Insert
            if (data.id != 0L) {
                operationsException(insertHasNon0IdMsg, methodMisuse)
            }
            val commonDTO = runInsert(data)
            notifier.trigger(ContextEvents.InsertComplete, commonDTO)
            dtoClass.toResult(commonDTO)
        }
        afterUpdated(result)
    }.resultOrException()

    suspend fun insert(dataModels: List<D>): ResultList<DTO, D> =
        runTaskAsync("Insert<List<${dataType.typeName}>>(${dataModels.size})", taskConfig) {

        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
      val result =  withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            val operation = CrudOperation.Insert
            val split = splitInputData(dataModels)
            if (split.insertList.isNotEmpty()) {
                split.insertList.forEach {
                    val commonDTO = runInsert(it)
                    resultingList.add(commonDTO)
                }
            }
            if (split.updateList.isNotEmpty()) {
                operationsException(insertHasNon0IdMsg, ExceptionCode.METHOD_MISUSED)
            }
            resultingList.toResult(dtoClass, operation)
        }
            afterUpdated(result)
    }.resultOrException()

    suspend fun update(dataModel: D): ResultSingle<DTO, D> = runTaskAsync("Update<${dataType.typeName}>", taskConfig) {
        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        val result = withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            if (dataModel.id == 0L) {
                val res = runInsert(dataModel)
                dtoClass.toResult(res)
            } else {
                val updateResult = runUpdate(dataModel)
                dtoClass.toResult(updateResult)
            }
        }
        afterUpdated(result)
    }.resultOrException()

    suspend fun update(dataModels: List<D>): ResultList<DTO, D> =
        runTaskAsync("Update<List<${dataType.typeName}>>", taskConfig) {
        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            val operation = CrudOperation.Update
            val split = splitInputData(dataModels)
            if (split.insertList.isNotEmpty()) {
                split.insertList.forEach {
                   val insertResult =  runInsert(it)
                    resultingList.add(insertResult)
                }
            }
            if (split.updateList.isNotEmpty()) {
                split.updateList.forEach {
                    val res = runUpdate(it)
                    resultingList.add(res)
                }
            }
            resultingList.toResult(dtoClass, operation)
        }
    }.resultOrException()

    override fun toString(): String = completeName
}

@ExecutionContextDSL
class DTOExecutionContext<DTO, D, E, F, FD, FE>(
    val hostDTO: CommonDTO<DTO, D, E>,
    val dtoClass: DTOClass<F, FD, FE>,
) : ExecutionContext<F, FD, FE>(dtoClass), TasksManaged
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    override val identity: CTXIdentity<DTOExecutionContext<DTO, D, E, F, FD, FE>> = asSubIdentity(hostDTO)

    init {
        identity.setNamePattern {
            "DTOExecutionContext[Host:${hostDTO.identifiedByName} For Class: ${dtoClass.identifiedByName}]"
        }
    }

    fun lookupExistent(id: Long): CommonDTO<F, FD, FE>? {
        return dtoList.firstOrNull { it.id == id }
    }

    fun dtoLookup(entity: FE): CommonDTO<F, FD, FE>? {
        return lookupExistent(entity.id.value)
    }

    private fun dtoLookup(id: Long): CommonDTO<F, FD, FE>? {
        return dtoList.firstOrNull { it.id == id }
    }


   internal fun restoreDTO(initiator: CTX, entity: E): List<CommonDTO<F, FD, FE>> {
        val result = reloadByEntity(initiator, entity)
        result.forEach { commonDTO ->
            commonDTO.bindingHub.relationDelegateMap.values.forEach { delegate ->
                commonDTO.withDTOContextCreating(delegate.dtoClass) {
                    val entity = commonDTO.entityContainer.getValue(this)
                    restoreDTO(this, entity)
                }
            }
            commonDTO.bindingHub.applyDataModels()
        }
        dtoList.addAll(result)
        return result
    }

    private fun runInsertInDTOContext(dataModel: FD, dtoClass: DTOBase<F, FD, FE>): List<CommonDTO<F, FD, FE>> {
        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        withHostDTOHub {
            val relationDelegates = getRelationDelegates(dtoClass)
            relationDelegates.forEach { delegate ->
                val commonDTO = delegate.createDTO(dataModel)

                withDTOHub(commonDTO) {
                    resolveParent(hostDTO)
                    val persistedEntity = daoService.save { entity ->
                        updateEntity(entity)
                    }
                    commonDTO.entityContainer.provideValue(persistedEntity)
                }
                resultingList.add(commonDTO)
            }
        }
        return resultingList
    }

    fun insert(initiator: CTX, dataModel: FD): ResultSingle<F, FD> =
        runTask("Insert<${dataType.typeName}>(id=${dataModel.id})"){
        val commonDTO = runInsertInDTOContext(dataModel, dtoClass).exactlyOneOrThrow {
            operationsException(wrongListSizeMsg, abnormalState)
        }
        dtoClass.toResult(commonDTO)
    }.resultOrException()

    fun insert(initiator: CTX): ResultList<F, FD> {
        "Insert now in context $identifiedByName".output(Colour.CyanBright)
        val result = insertChildBinding(initiator)
        result.forEach { commonDTO ->
            withDTOHub(commonDTO) {
                relationDelegateMap.values.forEach { delegate ->
                    commonDTO.withDTOContextCreating(delegate.dtoClass) {
                        insert(this)
                    }
                }
            }
        }
        dtoList.addAll(result)
        return dtoClass.toResult(result)
    }

    fun insert(initiator: CTX, dataModels: List<FD>): ResultList<F, FD> =
        runTask("Insert<List<${dataType.typeName}>>") {
        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        val split = splitInputData(dataModels)
        if (split.updateList.isNotEmpty()) {
            operationsException(insertHasNon0IdMsg, methodMisuse)
        }
        if (split.insertList.isNotEmpty()) {
            split.insertList.forEach { dataModel ->
                val commonDTOList = runInsertInDTOContext(dataModel, dtoClass)
                commonDTOList.forEach { commonDTO ->
                    withDTOHub(commonDTO) {
                        getRelationDelegatesPacked().delegates.forEach { delegate ->
                            hostingDTO.withDTOContextCreating(delegate.dtoClass) {
                                val casted =
                                    this@withDTOContextCreating.castOrOperations<DTOExecutionContext<DTO, D, E, F, FD, FE>>(
                                        this
                                    )
                                val castedList = delegate.extractDataModels()
                                    .castListOrManaged(this, casted.dtoClass.commonDTOType.dataType.kClass)
                                casted.insert(this, castedList)
                            }
                        }
                    }
                    dtoList.add(commonDTO)
                }
                resultingList.addAll(commonDTOList)
            }
        }
        dtoClass.toResult(resultingList)
    }.resultOrException()

    fun select(conditions: SimpleQuery): ResultList<F, FD> =
        runTask("Select<${dataType.typeName}>(SimpleQuery)") {
        trySubscribingHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {

            val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
            val entities = daoService.select(conditions)
            entities.forEach { entity ->
                val existent = lookupExistent(entity.id.value)
                if (existent == null) {
                    val commonDTO = dtoClass.newDTO()
                    withDTOHub(commonDTO) {
                        resolveParent(hostDTO)
                        resolveAttachedForeign(this, entity)
                        updateByEntity(entity)
                        getRelationDelegates(dtoClass).withEachDelegate {
                            hostingDTO.withDTOContextCreating(dtoClass) {
                                restoreDTO(this, entity)
                            }
                        }
                    }
                    dtoList.add(commonDTO)
                    resultingList.add(commonDTO)
                } else {
                    resultingList.add(existent)
                }
            }
            dtoClass.toResult(resultingList)
        }
    }.resultOrException()

    fun select(): ResultList<F, FD> = runTask("Select<${dataType.typeName}>(All)") {
        trySubscribingListHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {
            dtoClass.toResult(dtoList)
        }
    }.resultOrException()

    fun pick(id: Long): ResultSingle<F, FD> = runTask("Pick<${dataType.typeName}>(id=$id)") {
        trySubscribingHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = true) {
            val commonDTO =  lookupExistent(id)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<F, FD> =
        runTask("Pick<${dataType.typeName}>(SimpleQuery)") {
        trySubscribingHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = true) {
            daoService.pick(conditions)?.let { entity ->
                dtoClass.toResult(dtoLookup(entity))
            } ?: run {
                val notFound = operationsException(
                    notFoundByQueryMsg(conditions.build().toSqlString()),
                    ExceptionCode.DTO_LOOKUP_FAILURE
                )
                dtoClass.toResult(notFound)
            }
        }
    }.resultOrException()

    fun update(initiator: CTX): ResultList<F, FD> =
        runTask("Update<${dataType.typeName}>(All)") {
        withTransactionIfNone(hostDTO.dtoClass.debugger, warnIfNoTransaction = true) {
            withHostDTOHub {
                val dtoEntity = hostingDTO.entityContainer.getValue(this)
                hostingDAO.update(dtoEntity) { entity ->
                    updateEntity(entity)
                }
                getRelationDelegates(dtoClass).forEach { relationDelegate ->
                    val newDtos = relationDelegate.createDTOS { existent ->
                        "Existent triggered in update(Sub). Triggered existent $existent".output(Colour.Cyan)
                    }
                    newDtos.forEach { newDTO ->
                        withDTOHub(newDTO) {
                            val persistedEntity = daoService.save { entity ->
                                updateEntity(entity)
                            }
                            hostingDTO.entityContainer.provideValue(persistedEntity)
                            dtoList.add(newDTO)
                        }
                    }
                    dtoList.addAll(newDtos)
                }
            }
        }
        dtoList.toResult(dtoClass, CrudOperation.Update)
    }.resultOrException()

    fun update(initiator: CTX, dataModel:FD): ResultSingle<F, FD> =
        runTask("Update<${dataType.typeName}>(id=${dataModel.id})"){
        withTransactionIfNone(hostDTO.dtoClass.debugger) {
            val existent = lookupExistent(dataModel.id)
            existent?.bindingHub?.updateByData(dataModel)
            dtoClass.toResult(existent)
        }
    }.resultOrException()


    fun update(initiator: CTX, dataModels: List<FD>): ResultList<F, FD> =
        runTask("Update<List<${dataType.typeName}>>(${dataModels.size})") {
            trySubscribingListHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger) {
            val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
            val split =  splitInputData(dataModels)
            if (split.insertList.isNotEmpty()) {
                split.insertList.forEach {
                   val commonDTO =  runInsertInDTOContext(it, dtoClass)
                    resultingList.addAll(commonDTO)
                }
            }
            if (split.updateList.isNotEmpty()) {
                split.updateList.forEach {update->
                   dtoList.firstOrNull { it.id == update.id }?.let {found->
                       found.bindingHub.updateByData(update)
                       resultingList.add(found)
                   }
                }
            }
            dtoClass.toResult(resultingList)
        }
    }.resultOrException()

    override fun toString(): String = completeName
}

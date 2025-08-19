package po.exposify.dto.components.executioncontext

import kotlinx.coroutines.currentCoroutineContext
import org.jetbrains.exposed.dao.LongEntity
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
import po.exposify.dto.components.bindings.withHostDTOHub
import po.exposify.dto.components.bindings.withHub
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.toSqlString
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.extensions.withSuspendedTransactionIfNone
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
import po.misc.context.asSubIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.functions.registries.SubscriptionPack
import po.misc.types.TypeData
import po.misc.types.castListOrManaged

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
    private val dataList: List<D>
){
    val updateList : List<D> =  dataList.filter { it.id > 0L }
    val insertList: List<D> = dataList.filter { it.id <= 0L }
}

sealed class ExecutionContext<DTO, D, E>(
    private val dtoClass: DTOBase<DTO, D, E>,
) : TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    abstract override val identity: CTXIdentity<out CTX>

    //internal val notifier = taggedRegistryOf<ContextEvents, CommonDTO<DTO, D, E>>()
   // internal val listNotifier = taggedRegistryOf<ContextListEvents, List<CommonDTO<DTO, D, E>>>()

    private val daoService: DAOService<DTO, D, E> get() = dtoClass.dtoConfiguration.daoService

    protected val  dataType: TypeData<D> get() = dtoClass.commonDTOType.dataType

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
     //   notifier.identifiedByType(dtoClass.commonDTOType.dtoType)
      //  listNotifier.identifiedByType(dtoClass.commonDTOType.dtoType)
    }

//    protected fun restoreDTOWildcard(entities: List<*>):List<CommonDTO<DTO, D, E>> {
//        return withCastedOrManaged<List<E>, List<CommonDTO<DTO, D, E>>>(entities){
//            restoreDTOS(this)
//        }
//    }



//        if(this is DTOExecutionContext<DTO, D, E, *, *, *>){
//            hostingDTO.bindingHub.attachDTOS(resultingList, dtoClass.commonDTOType)
//        }
     //   return resultingList
   // }


    suspend fun delete(dataModel: D): ResultSingle<DTO, D>  = runTaskAsync("Delete(All)") {
        val currentContext =  currentCoroutineContext()
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {

        }
        TODO("Not yet implemented")
    }.resultOrException()

    protected fun trySubscribingHooks(process: Process<AuthorizedSession>?) {
       process?.let {
           val hooks = it.receiver.getExternalRef<SubscriptionPack<CommonDTO<DTO, D, E>>>(ContextEvents)
           if (hooks != null) {
             //  notifier.trySubscribe(hooks)
           }
       }
   }
    protected fun trySubscribingListHooks(process: Process<AuthorizedSession>?) {
        process?.let {
            val hooks = it.receiver.getExternalRef<SubscriptionPack<List<CommonDTO<DTO, D, E>>>>(ContextListEvents)
            if (hooks != null) {
               // listNotifier.trySubscribe(hooks)
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

    override val identity: CTXIdentity<RootExecutionContext<DTO, D, E>> = asSubIdentity(this, dtoClass)
    private val daoService get() = dtoClass.dtoConfiguration.daoService
    private val taskConfig = TaskConfig(taskType = TaskType.AsRootTask)


    init {
        identity.setNamePattern { "RootExecutionContext[${dtoClass.identifiedByName}]" }
    }

   suspend fun restoreDTO(dataModel:D): CommonDTO<DTO, D, E> {
       return insert(dataModel).getDTOForced().castOrOperations(this)
    }

   fun restoreDTO(entity:E): CommonDTO<DTO, D, E> {
       val dto = dtoClass.newDTO()
       withHub(dto){
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

   suspend fun restoreDTO(dto: CommonDTO<DTO, D, E>): CommonDTO<DTO, D, E> {
        var result : CommonDTO<DTO, D, E>? = null

        if(dto.id > 0){
          val existentDTO =  dtoLookupInExistent(dto.id)
          if(existentDTO != null){
              result = existentDTO
          }
        }
        if(dto.entityContainer.value != null){
            result =  restoreDTO(dto.entityContainer.value!!)
        }

       val dataModel = dto.dataContainer.value
        if(dataModel != null){
            result =  restoreDTO(dataModel)
            dto.entityContainer.provideValue(result.entityContainer.getValue(this))
            dto.dataContainer.provideValue(result.dataContainer.getValue(this))
            dtoList.add(dto)
            result =  dto
        }
        return result.getOrOperations(this)
    }

    private  fun dtoLookup(entity: E): CommonDTO<DTO, D, E> {
        var existent = dtoLookupInExistent(entity.id.value)
        if (existent == null) {
            existent = restoreDTO(entity)
        }
        return existent.getOrOperations(this)
    }

    internal fun dtoLookupInExistent(id: Long):  CommonDTO<DTO, D, E>? {
        return dtoList.firstOrNull { it.id == id }
    }

    private fun runInsert(
        dataModel: D
    ): CommonDTO<DTO, D, E> {

        val commonDTO = dtoClass.newDTO(dataModel)
        if (commonDTO.dataStatus == DataStatus.PreflightCheckMock) {
            return commonDTO
        }
        withHub(commonDTO) { dto ->
            resolveAttachedForeign(this, dataModel)
            val executionCTX = this@RootExecutionContext
            val persistedEntity = dto.daoService.save { entity ->
                updateEntity(entity)
            }
            persistedEntity.flush()
            dto.entityContainer.provideValue(persistedEntity)

            relationDelegateMap.values.forEach { childDelegate ->
                dto.withDTOContextCreating(childDelegate.dtoClass) {
                    insert(this,  childDelegate.extractDataModels().castListOrManaged(dtoClass.commonDTOType.dataType.kClass, this))
                }
            }
        }
        dtoList.add(commonDTO)
        return commonDTO
    }

    private suspend fun runUpdate(
        dataModel:D
    ): CommonDTO<DTO, D, E> {

        val commonDTO = dtoLookupInExistent(dataModel.id) ?: run { restoreDTO(dataModel) }

        withHub(commonDTO) {
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

            val selection = dtoList

            dtoClass.toResult(dtoList.toList())
        }
    }.resultOrException()

    suspend fun select(conditions: SimpleQuery): ResultList<DTO, D> = runTaskAsync("select<SimpleQuery>", taskConfig) {
        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val operation = CrudOperation.Select
            val resultingList = mutableListOf<CommonDTO<DTO, D, E>>()
            daoService.select(conditions).forEach { entity ->
                val commonDTO = dtoLookup(entity)
                resultingList.add(commonDTO)
            }
            dtoClass.toResult(resultingList)
        }
    }.resultOrException()

    suspend fun pick(id: Long): ResultSingle<DTO, D> = runTaskAsync("Pick<$id>", taskConfig) {
        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentCoroutineContext()) {
            val commonDTO = dtoLookupInExistent(id)?:run {
                 daoService.pickById(id)?.let {  restoreDTO(it)  }
            }
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    suspend fun pick(conditions: SimpleQuery): ResultSingle<DTO, D> = runTaskAsync("Pick<SimpleQuery>", taskConfig) {
        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val operation = CrudOperation.Pick
            daoService.pick(conditions)?.let { entity ->
                val commonDTO = dtoLookupInExistent(entity.id.value)
              //  commonDTO?.let { notifier.trigger(ContextEvents.PickComplete, it) }
                dtoClass.toResult(commonDTO)
            } ?: run {
                val notFound = operationsException(
                    notFoundByQueryMsg(conditions.build().toSqlString()),
                    ExceptionCode.DTO_LOOKUP_FAILURE
                )
                dtoClass.toResult(notFound)
            }
        }
    }.resultOrException()

    suspend fun insert(data: D): ResultSingle<DTO, D> = runTaskAsync("Insert<${dataType.typeName}>", taskConfig) {
        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process<AuthorizedSession>())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            val operation = CrudOperation.Insert
            if (data.id != 0L) {
                operationsException(insertHasNon0IdMsg, methodMisuse)
            }
            val commonDTO = runInsert(data)
           // notifier.trigger(ContextEvents.InsertComplete, commonDTO)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    suspend fun insert(dataModels: List<D>): ResultList<DTO, D> = runTaskAsync("Insert<List<${dataType.typeName}>>", taskConfig) {

        val currentContext = currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
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
    }.resultOrException()

    suspend fun update(dataModel: D): ResultSingle<DTO, D> =
        runTaskAsync("Update<List<${dataType.typeName}>>", taskConfig) {
        val currentContext =  currentCoroutineContext()
        trySubscribingHooks(currentContext.process())
        withSuspendedTransactionIfNone(dtoClass.debugger, false, currentContext) {
            if (dataModel.id == 0L) {
                val res = runInsert(dataModel)
                dtoClass.toResult(res)
            } else {
                val updateResult =  runUpdate(dataModel)
                dtoClass.toResult(updateResult)
            }
        }
    }.resultOrException()

    suspend fun update(dataModels: List<D>): ResultList<DTO, D> = runTaskAsync("Update<List<${dataType.typeName}>>", taskConfig) {
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
    override val identity: CTXIdentity<DTOExecutionContext<DTO, D, E, F, FD, FE>> = asSubIdentity(this, hostDTO)


    internal val dtoFactory: DTOFactory<F, FD, FE> by lazy { DTOFactory(dtoClass.dtoConfiguration) }
    private val daoService get() = dtoClass.dtoConfiguration.daoService
    private val commonDTOType get() = dtoClass.commonDTOType

    init {
        identity.setNamePattern { "DTOExecutionContext[Host:${hostDTO.identifiedByName} For: ${dtoClass.identifiedByName}]" }
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

    fun restoreDTO(initiator: CTX, entity: E): List<CommonDTO<F, FD, FE>> {
        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        hostDTO.bindingHub.getRelationDelegates(dtoClass).withEachDelegate { delegateClass ->
            createDTOS(entity).forEach { commonDTO ->
                dtoList.add(commonDTO)
                commonDTO.withDTOContextCreating(dtoClass) {
                    restoreDTO(this, commonDTO.entityContainer.getValue(this))
                }
                resultingList.add(commonDTO)
            }
        }
        return resultingList
    }

    fun restoreDTO(initiator: CTX) {

    }


    private fun runInsertInDTOContext(
        initiator: CTX,
        dataModel: FD,
        dtoClass: DTOBase<F, FD, FE>
    ): List<CommonDTO<F, FD, FE>> {

        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        withHostDTOHub {
            val relationDelegates = getRelationDelegates(dtoClass)
            relationDelegates.forEach { delegate ->
                val commonDTO = delegate.createDTO(dataModel)
                withHub(commonDTO) {
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

    fun insert(initiator: CTX, dataModel: FD): ResultSingle<F, FD> {
        val commonDTO = runInsertInDTOContext(initiator, dataModel, dtoClass).exactlyOneOrThrow {
            operationsException(wrongListSizeMsg, abnormalState)
        }
        return dtoClass.toResult(commonDTO)
    }

    fun insert(initiator: CTX, dataModels: List<FD>): ResultList<F, FD> {

        val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
        val split = splitInputData(dataModels)
        if (split.updateList.isNotEmpty()) {
            operationsException(insertHasNon0IdMsg, methodMisuse)
        }
        if (split.insertList.isNotEmpty()) {
            split.insertList.forEach { dataModel ->
                val commonDTOList = runInsertInDTOContext(initiator, dataModel, dtoClass)
                commonDTOList.forEach { commonDTO ->
                    withHub(commonDTO) {
                        getRelationDelegatesPacked().delegates.forEach { delegate ->
                            hostingDTO.withDTOContextCreating(delegate.dtoClass) {
                                val casted =
                                    this@withDTOContextCreating.castOrOperations<DTOExecutionContext<DTO, D, E, F, FD, FE>>(
                                        this
                                    )
                                val castedList = delegate.extractDataModels()
                                    .castListOrManaged(casted.dtoClass.commonDTOType.dataType.kClass, this)
                                casted.insert(this, castedList)
                            }
                        }
                    }
                    dtoList.add(commonDTO)
                }
                resultingList.addAll(commonDTOList)
            }
        }
        return dtoClass.toResult(resultingList)
    }

    fun select(conditions: SimpleQuery): ResultList<F, FD> = runTask("Select(Conditions)") {
        trySubscribingHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {

            dtoList.forEach {
                it.output(Colour.CYAN)

            }

            val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
            val entities = daoService.select(conditions)
            entities.forEach { entity ->
                val existent = lookupExistent(entity.id.value)
                if (existent == null) {
                    val commonDTO = dtoClass.newDTO()
                    withHub(commonDTO) {
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

    fun select(): ResultList<F, FD> = runTask("Select(All)") {
        trySubscribingListHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = false) {
            dtoClass.toResult(dtoList)
        }
    }.resultOrException()


    fun pick(id: Long): ResultSingle<F, FD> = runTask("Pick<SimpleQuery>") {
        trySubscribingHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = true) {
            val commonDTO =  lookupExistent(id)
            dtoClass.toResult(commonDTO)
        }
    }.resultOrException()

    fun pick(conditions: SimpleQuery): ResultSingle<F, FD> = runTask("Pick<SimpleQuery>") {
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


    fun update(
        initiator: CTX
    ): ResultList<F, FD> = runTask("Update<${dataType.typeName}>(id = ${hostDTO.dataContainer.getValue(this).id})") {

            withTransactionIfNone(hostDTO.dtoClass.debugger, warnIfNoTransaction = true) {
                 withHostDTOHub {
                     val dtoEntity = hostingDTO.entityContainer.getValue(this)
                     hostingDAO.update(dtoEntity) {entity->
                         updateEntity(entity)
                     }
                     getRelationDelegates(dtoClass).forEach {relationDelegate->
                         val newDtos = relationDelegate.createDTOS {existent->
                            "Existent triggered in update(Sub). Triggered existent $existent".output(Colour.CYAN)
                         }
                         newDtos.forEach {newDTO->
                             withHub(newDTO){
                                 val persistedEntity = daoService.save {entity->
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


    fun update(
        initiator: CTX,
        dataModel:FD
    ): ResultSingle<F, FD> = runTask("Update<${hostDTO.dtoClass.commonDTOType.dataType.typeName}>(id = ${dataModel.id})") {

        withTransactionIfNone(hostDTO.dtoClass.debugger, warnIfNoTransaction = true) {
            val existent = lookupExistent(dataModel.id)
            existent?.bindingHub?.updateByData(dataModel)
            dtoClass.toResult(existent)
        }
    }.resultOrException()


    fun update(
        initiator: CTX,
        dataModels: List<FD>
    ): ResultList<F, FD> = runTask("Update<List<${dataType.typeName}>>(${dataModels.size})") {
            trySubscribingListHooks(activeProcess())
        withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = true) {
            val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
            val split =  splitInputData(dataModels)
            if (split.insertList.isNotEmpty()) {
                split.insertList.forEach {
                   val commonDTO =  runInsertInDTOContext(initiator, it, dtoClass)
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

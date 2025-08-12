package po.exposify.scope.sequence.builder

import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.withDTOContext
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResultList
import po.exposify.dto.components.result.toResultSingle
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTaskAsync
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.LambdaHolder
import po.misc.types.TypeData


enum class ChunkType {
    InputType,
    OutputType,
}


sealed class ChunkBase<DTO, D, R> : CTX where DTO : ModelDTO, D : DataModel, R : Any {
    abstract val chunkType: ChunkType
    val withInputValueLambda: LambdaHolder<D> = LambdaHolder(this)

    val switchContainers: MutableMap<TypeData<*>, SwitchChunkContainer<*, *, DTO, D>> = mutableMapOf()

    var parameterCallback: ((Long) -> R)? = null

    fun subscribeParameter(callback: (Long) -> R) {
        parameterCallback = callback
    }
    var queryCallback: ((DeferredContainer<WhereQuery<*>>) -> R)? = null

    fun subscribeQuery(callback: (DeferredContainer<WhereQuery<*>>) -> R) {
        queryCallback = callback
    }

    var dataInputCallback: ((D) -> R)? = null
    fun subscribeData(callback: (D) -> R) {
        dataInputCallback = callback
    }

    var listDataInputCallback: ((List<D>) -> R)? = null
    fun subscribeListData(callback: (List<D>) -> R) {
        listDataInputCallback = callback
    }

    var noParamCallback: (() -> R)? = null

    fun subscribeNoParam(callback: () -> R) {
        noParamCallback = callback
    }

    protected abstract fun onResultReady(result: R)

    abstract fun configure()

    override fun toString(): String = identifiedByName
}

sealed class SingleResultChunks<DTO : ModelDTO, D : DataModel>(
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
) : ChunkBase<DTO, D, ResultSingle<DTO, D>>(), TasksManaged {


    val resultContainer: DeferredContainer<ResultSingle<DTO, D>> = DeferredContainer(this)
    val configContainer: LambdaHolder<SingleResultChunks<DTO, D>> = LambdaHolder(this)

    init {
        configContainer.registerProvider(configurationBlock)
    }

    var resultSubscription: ((ResultSingle<DTO, D>) -> Unit)? = null

    fun subscribeForResult(subscription: (ResultSingle<DTO, D>) -> Unit) {
        resultSubscription = subscription
    }

    override fun onResultReady(result: ResultSingle<DTO, D>) {
        resultSubscription?.invoke(result)
    }

    fun unsubscribeResult() {
        resultSubscription = null
    }

    override fun configure() {
        configContainer.resolve(this)
    }


    suspend fun triggerSingle(dtoClass: RootDTO<DTO, D, *>,  query: DeferredContainer<WhereQuery<*>>): ResultSingle<DTO, D> {
        return when (this) {
            is PickChunk -> {
                pick(dtoClass, query.resolve())
            }
            is UpdateChunk -> {
                val exception =  operationsException("triggerSingle method wrong usage", ExceptionCode.ABNORMAL_STATE)
                exception.toResultSingle(dtoClass, CrudOperation.Pick)
            }
        }
    }

    suspend fun triggerSingle(dtoClass: RootDTO<DTO, D, *>, id: Long): ResultSingle<DTO, D> {
       return when (this) {
            is PickChunk -> {
                   pickById(dtoClass, id)
            }
            is UpdateChunk -> {
                val exception =  operationsException("triggerSingle method wrong usage", ExceptionCode.ABNORMAL_STATE)
                exception.toResultSingle(dtoClass, CrudOperation.Pick)
            }
        }
    }

  suspend fun triggerSingle(dtoClass: RootDTO<DTO, D, *>,  inputData: D): ResultSingle<DTO, D> {
     return  when (this) {
           is UpdateChunk -> {
               update(dtoClass, inputData)
           }
           is PickChunk-> {
              val exception =  operationsException("triggerSingle method wrong usage", ExceptionCode.ABNORMAL_STATE)
              exception.toResultSingle(dtoClass, CrudOperation.Pick)
           }
       }
   }
}

sealed class ListResultChunks<DTO, D>(
    configurationBlock: ListResultChunks<DTO, D>.() -> Unit,
) : ChunkBase<DTO, D, ResultList<DTO, D>>(), TasksManaged where DTO : ModelDTO, D : DataModel {
    val resultContainer: DeferredContainer<ResultList<DTO, D>> = DeferredContainer(this)
    val configContainer: LambdaHolder<ListResultChunks<DTO, D>> = LambdaHolder(this)
    val withResultContainer: LambdaHolder<ResultList<DTO, D>> = LambdaHolder(this)

    init {
        configContainer.registerProvider(configurationBlock)
    }

    var resultSubscription: ((ResultList<DTO, D>) -> Unit)? = null

    fun subscribeForResult(subscription: (ResultList<DTO, D>) -> Unit) {
        resultSubscription = subscription
    }

    override fun onResultReady(result: ResultList<DTO, D>) {
        resultSubscription?.invoke(result)
    }

    override fun configure() {
        configContainer.resolve(this)
    }

    fun dataInputProvided(dataInput: List<D>): ResultList<DTO, D> {
        TODO("Not yet")
    }

    suspend fun triggerList(dtoClass: RootDTO<DTO, D, *>, dataModels: List<D>): ResultList<DTO, D> {
        return when (this) {
            is SelectChunk -> {
                val exception =  operationsException("triggerList method wrong usage", ExceptionCode.ABNORMAL_STATE)
                exception.toResultList(dtoClass, CrudOperation.Pick)
            }
            is UpdateListChunk -> {
                update(dtoClass, dataModels)
            }
        }
    }

    suspend fun triggerList(dtoClass: RootDTO<DTO, D, *>,  query: DeferredContainer<WhereQuery<*>>): ResultList<DTO, D> {
        return when (this) {
            is SelectChunk -> {
                select(dtoClass, query)
            }

            is UpdateListChunk<*, *> -> {
                val exception =  operationsException("triggerList method wrong usage", ExceptionCode.ABNORMAL_STATE)
                exception.toResultList(dtoClass, CrudOperation.Pick)
            }
        }
    }

    suspend fun triggerList(dtoClass: RootDTO<DTO, D, *>): ResultList<DTO, D> {
        return when (this) {
            is SelectChunk -> {
                select(dtoClass)
            }
            is UpdateListChunk<*, *> -> {
                val exception =  operationsException("triggerList method wrong usage", ExceptionCode.ABNORMAL_STATE)
                exception.toResultList(dtoClass, CrudOperation.Pick)
            }
        }
    }

}

class PickChunk<DTO, D>(
    val commonDTOType: CommonDTOType<DTO, D, *>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
) : SingleResultChunks<DTO, D>(configurationBlock),
    TasksManaged where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<PickChunk<DTO, D>> = asIdentity()
    override val chunkType: ChunkType = ChunkType.OutputType

    init {
        identity.setNamePattern { "PickChunk<${commonDTOType.dtoType.typeName}, ${commonDTOType.dataType.typeName}>" }
    }


    suspend fun pick(
        ownDTOClass: RootDTO<DTO, D, *>,
        conditions: SimpleQuery
    ): ResultSingle<DTO, D> =
        runTaskAsync("pickById") {
            ownDTOClass.executionContext.pick(conditions)
        }.resultOrException()

    suspend fun pickById(ownDTOClass: RootDTO<DTO, D, *>, id: Long): ResultSingle<DTO, D> =
        ownDTOClass.executionContext.pick(id)


    fun <F : ModelDTO, FD : DataModel> pickSwitching(
        parentResult: ResultSingle<F, FD>,
        ownDTOClass: DTOClass<DTO, D, *>,
        parameter: Long,
    ): ResultSingle<DTO, D> {
        return withDTOContextCreating(parentResult.getAsCommonDTO(), ownDTOClass) {
            pick(parameter)
        }
    }


    fun <F : ModelDTO, FD : DataModel> pickSwitching(
        parentResult: ResultSingle<F, FD>,
        ownDTOClass: DTOClass<DTO, D, *>,
        whereQuery: DeferredContainer<WhereQuery<*>>,
    ): ResultSingle<DTO, D> {
        return withDTOContextCreating(parentResult.getAsCommonDTO(), ownDTOClass) {
            pick(whereQuery.resolve())
        }
    }
}

class UpdateChunk<DTO, D>(
    val commonDTOType: CommonDTOType<DTO, D, *>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
) : SingleResultChunks<DTO, D>(configurationBlock) where DTO : ModelDTO, D : DataModel {

    override val identity: CTXIdentity<UpdateChunk<DTO, D>> = asIdentity()
    override val chunkType: ChunkType = ChunkType.InputType

    init {
        identity.setNamePattern { "UpdateChunk<${commonDTOType.dtoType.typeName}, ${commonDTOType.dataType.typeName}>" }
    }

    suspend fun update(
        dtoClass: RootDTO<DTO, D, *>,
        input: D
    ): ResultSingle<DTO, D> = dtoClass.executionContext.update(input)


    suspend fun <F : ModelDTO, FD : DataModel> updateSwitching(
        parentResult: ResultSingle<F, FD>,
        ownDTOClass: DTOClass<DTO, D, *>,
        input: D
    ): ResultSingle<DTO, D> =
        runTaskAsync("updateSwitching") {
            val result = withDTOContextCreating(parentResult.getAsCommonDTO(), ownDTOClass) {
                update(input, this)
            }
            result
        }.resultOrException()
}

class SelectChunk<DTO, D>(
    val commonDTOType: CommonDTOType<DTO, D, *>,
    configBlock: ListResultChunks<DTO, D>.() -> Unit,
) : ListResultChunks<DTO, D>(configBlock) where DTO : ModelDTO, D : DataModel {

    override val identity: CTXIdentity<SelectChunk<DTO, D>> = asIdentity()
    override val chunkType: ChunkType = ChunkType.OutputType

    init {
        identity.setNamePattern { "SelectChunk<${commonDTOType.dtoType.typeName}, ${commonDTOType.dataType.typeName}>" }
    }

    suspend fun select(dtoClass: RootDTO<DTO, D, *>): ResultList<DTO, D> =
        runTaskAsync("update") {
            dtoClass.executionContext.select()
        }.resultOrException()

    suspend fun select(dtoClass: RootDTO<DTO, D, *>, query: DeferredContainer<WhereQuery<*>>): ResultList<DTO, D> =
        runTaskAsync("update(query)") {
            dtoClass.executionContext.select(query.resolve())
        }.resultOrException()


    fun <F : ModelDTO, FD : DataModel> selectSwitching(
        parentDTO: CommonDTO<F, FD, *>,
        ownDTOClass: DTOClass<DTO, D, *>

    ): ResultList<DTO, D> {

        return withDTOContextCreating(parentDTO, ownDTOClass) {
            select()
        }
    }

    fun <F : ModelDTO, FD : DataModel> selectSwitching(
        parentResult: CommonDTO<F, FD, *>,
        ownDTOClass: DTOClass<DTO, D, *>,
        whereQuery: DeferredContainer<WhereQuery<*>>,
    ): ResultList<DTO, D> {
        return withDTOContextCreating(parentResult,  ownDTOClass) {
            select(whereQuery.resolve())
        }
    }
}

class UpdateListChunk<DTO, D>(
    val commonDTOType: CommonDTOType<DTO, D, *>,
    configBlock: ListResultChunks<DTO, D>.() -> Unit,
) : ListResultChunks<DTO, D>(configBlock) where DTO : ModelDTO, D : DataModel {
    override val identity: CTXIdentity<UpdateListChunk<DTO, D>> = asIdentity()

    override val chunkType: ChunkType = ChunkType.InputType
    override val contextName: String get() = "UpdateListChunk"

    init {
        identity.setNamePattern { "UpdateListChunk<${commonDTOType.dtoType.typeName}, ${commonDTOType.dataType.typeName}>" }
    }

    suspend fun update(
        dtoClass: RootDTO<DTO, D, *>,
        input: List<D>
    ): ResultList<DTO, D> = dtoClass.executionContext.update(input)


    suspend fun <F : ModelDTO, FD : DataModel> updateSwitching(
        parentResult: ResultSingle<F, FD>,
        ownDTOClass: DTOClass<DTO, D, *>,
        input:List<D>
    ): ResultList<DTO, D> =
        runTaskAsync("updateSwitching") {
            val result = withDTOContextCreating(parentResult.getAsCommonDTO(),  ownDTOClass) {
                update(input, this)
            }
            result
        }.resultOrException()

}

package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.ExecutionProvider
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.isTransactionReady
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import kotlin.coroutines.coroutineContext


class SequenceContext<DTO, DATA, ENTITY>(
    val executionContext: ExecutionContext<DTO, DATA, ENTITY>
): TasksManaged, IdentifiableComponent where  DTO : ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val qualifiedName: String get() = "SequenceContext[${executionContext.hostingDTO.dtoName}]"
    override val name: String  get() = "SequenceContext"

    private fun dtos(): List<CommonDTO<DTO, DATA, LongEntity>>{
      return  lastResult.rootDTOs
    }

    private suspend fun notifyOnStart(method: String){
        val session =  coroutineContext[AuthorizedSession]
       // handler.onStartCallback?.invoke(RunnableContext.createRunInfo(method, session))
    }

    private suspend fun notifyOnComplete(method: String){
        val session =  coroutineContext[AuthorizedSession]
        //handler.onCompleteCallback?.invoke(RunnableContext.createRunInfo(method, session))
    }

    private fun lastResultSingleToMultiple(dto : CommonDTO<DTO, DATA, LongEntity>): ResultList<DTO, DATA>{
      return  ResultList(listOf(dto))
    }

    private var resultCallback: ((ResultList<DTO, DATA>) -> Unit)? = null
    var lastResult: ResultList<DTO, DATA> = ResultList()

    fun onResult(callback: (ResultList<DTO, DATA>) -> Unit) {
        resultCallback = callback
    }

    fun checkout(result: ResultList<DTO, DATA>) {
        lastResult = result
        resultCallback?.invoke(result)
    }
    fun getResult(): ResultList<DTO, DATA> = lastResult


    suspend fun checkout(withResult :  ResultList<DTO, DATA>? = null): ResultList<DTO, DATA> {
        return lastResult
    }

    suspend fun checkout(dtos :  List<CommonDTO<DTO, DATA, LongEntity>>): ResultList<DTO, DATA> {
        return ResultList(dtos.toList())
    }

    suspend fun checkout(dto :  CommonDTO<DTO, DATA, LongEntity>): ResultSingle<DTO, DATA> {
        return ResultSingle(dto)
    }

    suspend fun <C_DTO: ModelDTO, CD: DataModel> switch(
        childDtoClass: DTOClass<C_DTO, CD>,
        sequenceID: SequenceID,
        id: Long,
        block: suspend SequenceContext<C_DTO, CD, LongEntity>.(SequenceHandler<C_DTO, CD>)-> Unit
    ){
      //  val switchCondition = SwitchQuery<IdTable<Long>>().equalsTo(column,  childDtoClass.config.entityModel[id].id )
        val dto = executionContext.pickById(childDtoClass, id).getDTO().getOrOperationsEx("Dto not found for id :${id}")
        val newExecutionContext = ExecutionProvider<C_DTO, CD, LongEntity>(dto)
        val newSequenceContext = SequenceContext<C_DTO, CD, LongEntity>(newExecutionContext)
        val handler = childDtoClass.createHandler(sequenceID)
        block.invoke(newSequenceContext, handler)
    }

    suspend fun <T: IdTable<Long>> pick(
        conditions: WhereQuery<T>,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dto: CommonDTO<DTO, DATA, LongEntity>?)-> Unit)? = null
    ): ResultSingle<DTO, DATA> {
        notifyOnStart("pick")
        val result =  executionContext.pick(conditions)
        lastResult =  lastResultSingleToMultiple(result.rootDTO!!)

        if (block != null) {
            this.block(result.rootDTO!!)
        } else {
            checkout(lastResult)
            notifyOnComplete("pick")
        }
       return ResultSingle(result.rootDTO!!)
    }

    suspend fun <T: IdTable<Long>> select(
        conditions: WhereQuery<T>?,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dtos: List<CommonDTO<DTO, DATA, LongEntity>>)-> Unit)? = null
    ):ResultList<DTO, DATA> = subTask("Select", qualifiedName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("select(With conditions)")
            lastResult = if (conditions != null) {
                executionContext.select(conditions)
            } else {
                executionContext.select()
            }
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("select")
            } else {
                checkout(lastResult)
                notifyOnComplete("select")
            }
            ResultList(lastResult.rootDTOs)
        }.resultOrException()

    suspend fun select(
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dtos: List<CommonDTO<DTO, DATA, LongEntity>>)->Unit)? = null
    ): ResultList<DTO, DATA> = subTask("Select", qualifiedName) { handler->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
             notifyOnStart("select")
              lastResult = executionContext.select()
            val a  = 10
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("select")
            } else {
                checkout(lastResult)
                notifyOnComplete("select")
            }
            lastResult
        }.resultOrException()


    suspend fun update(
        dto: CommonDTO<DTO, DATA, LongEntity>,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dto: CommonDTO<DTO, DATA, LongEntity>)-> Unit)? = null)
    {
        subTask("Update", qualifiedName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("update")
            val result = executionContext.update(dto.dataModel)
            println("update completed with result ${result.getData()}")

            val dto =  result.rootDTO
            if(dto != null){
                lastResult =  lastResultSingleToMultiple(result.getDTO()!!)
                if (block != null) {
                    this.block(dto)
                    notifyOnComplete("update")
                } else {
                    checkout(lastResult)
                    notifyOnComplete("update")
                }
            }
        }
    }

    @JvmName("updateDtos")
    suspend fun update(
        dtos: List<CommonDTO<DTO, DATA, LongEntity>>,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dtos: List<CommonDTO<DTO, DATA, LongEntity>>)-> Unit)? = null)
    {
        subTask("Update", qualifiedName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("update")
            val result =  executionContext.update(dtos.map { it.dataModel })
            println("update completed with result ${result.getDTO().count()}")
            lastResult  = result
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("update")
            } else {
                checkout(lastResult)
                notifyOnComplete("update")
            }
        }
    }

    suspend fun update(
        dataModels: DATA,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dto: CommonDTO<DTO, DATA, LongEntity>)-> Unit)? = null
    ): ResultList<DTO, DATA>  =  subTask("Update", qualifiedName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("update")
            val result = executionContext.update(dataModels)
            println("update completed with result ${result.getData()}")
            val dto =  result.rootDTO
            if(dto != null){

                lastResult = lastResultSingleToMultiple(result.getDTO()!!)

                if (block != null) {
                    this.block(dto)
                    notifyOnComplete("update")
                } else {
                    checkout(lastResult)
                    notifyOnComplete("update")
                }
            }

            lastResult
        }.resultOrException()

    @JvmName("updateDataModels")
    suspend fun update(
        dataModels: List<DATA>,
        block: (suspend SequenceContext<DTO, DATA, ENTITY>.(dtos: List<CommonDTO<DTO, DATA, LongEntity>>)-> ResultList<DTO, DATA>)? = null
    ): ResultList<DTO, DATA>
        = subTask("Update", qualifiedName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("update")

            lastResult =  executionContext.update(dataModels)
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("update")
            } else {
                checkout(lastResult)
                notifyOnComplete("update")
            }
            lastResult
        }.resultOrException()

}


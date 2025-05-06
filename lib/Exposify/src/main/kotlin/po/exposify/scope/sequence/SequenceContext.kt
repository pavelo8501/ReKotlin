package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ClassExecutionProvider
import po.exposify.dto.components.Query
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ExecutionContext2
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.ClassSequencePack
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.collections.generateKey


class SequenceContext<DTO, DATA, ENTITY>(
    val executionContext: ExecutionContext2<DTO, DATA, ENTITY>
): TasksManaged, IdentifiableComponent where  DTO : ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val qualifiedName: String get() = "SequenceContext[${executionContext.providerName}]"
    override val name: String  get() = "SequenceContext"

    suspend fun <C_DTO: ModelDTO, CD: DataModel> switch(
        childDtoClass: DTOClass<C_DTO, CD>,
        sequenceId : SequenceID,
        block: suspend SequenceContext<C_DTO, CD, LongEntity>.(ClassSequenceHandler<C_DTO, CD>)-> Unit
    ){
        val pack = ClassSequencePack(childDtoClass.generateKey(sequenceId), childDtoClass, block)
        if(childDtoClass.parentClass is RootDTO){
            childDtoClass.parentClass.getServiceClass().addSequencePack(pack)
            val newExecutionContext = ClassExecutionProvider<C_DTO, CD, LongEntity>(childDtoClass)
            val newSequenceContext = SequenceContext(newExecutionContext as ExecutionContext2<C_DTO, CD, LongEntity>)
            val classHandler = childDtoClass.createHandler(sequenceId)
            block.invoke(newSequenceContext, classHandler)
        }else{
            throw OperationsException("Switch. parent class is no RootDTO", ExceptionCode.INVALID_DATA)
        }
    }

    internal var onResultUpdated : ((ResultList<DTO,DATA>)-> Unit)?  = null
    private val latestResult : ResultList<DTO,DATA> = ResultList()
    internal fun submitLatestResult(result :  ResultList<DTO,DATA>):ResultList<DTO,DATA>{
       latestResult.fromListResult(result)
       onResultUpdated?.invoke(result)
       return latestResult
    }
    internal fun submitLatestResult(result :  ResultSingle<DTO,DATA>): ResultSingle<DTO,DATA>{
        latestResult.fromSingleResult(result)
        onResultUpdated?.invoke(latestResult)
        return result
    }

    suspend fun <T: IdTable<Long>> pick(
        conditions: WhereQuery<T>,
    ): ResultSingle<DTO, DATA> = subTask("Pick", qualifiedName) { handler ->
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun <T: IdTable<Long>> pickById(
        id: Long
    ): ResultSingle<DTO, DATA> = subTask("PickById", qualifiedName) { handler ->
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(
        conditions: Query,
    ):ResultList<DTO, DATA> = subTask("Select", qualifiedName) { handler ->
       val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(
    ):ResultList<DTO, DATA> = subTask("Select", qualifiedName) { handler ->
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(
        dataModels: List<DATA>
    ):ResultList<DTO, DATA> = subTask("Update", qualifiedName) { handler ->
        val result = executionContext.update(dataModels)
        submitLatestResult(result)
    }.resultOrException()

}


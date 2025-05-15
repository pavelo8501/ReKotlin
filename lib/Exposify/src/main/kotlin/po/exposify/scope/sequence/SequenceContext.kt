package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask


class SequenceContext<DTO, DATA, ENTITY>(
   internal val sequenceHandler: SequenceHandlerBase<DTO, DATA, ENTITY>,
   private val executionContext: ExecutionContext<DTO, DATA, ENTITY>
): TasksManaged, IdentifiableComponent where  DTO : ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val qualifiedName: String get() = "SequenceContext[${executionContext.providerName}]"
    override val name: String  get() = "SequenceContext"

    private var latestSingleResult : ResultSingle<DTO,DATA, ENTITY> = ResultSingle(sequenceHandler.dtoBase)
    internal val lastResultProvider : ()-> ResultSingle<DTO, DATA, ENTITY> = {
        latestSingleResult
    }

    internal var onResultUpdated : ((ResultList<DTO, DATA, ENTITY>)-> Unit)?  = null

    internal fun submitLatestResult(result :  ResultList<DTO, DATA, ENTITY>):ResultList<DTO, DATA, ENTITY>{
       sequenceHandler.provideFinalResult(result)
       onResultUpdated?.invoke(result)
       return result
    }
    internal fun submitLatestResult(result :  ResultSingle<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY>{
        latestSingleResult = result
        sequenceHandler.provideFinalResult(ResultList<DTO, DATA, ENTITY>(sequenceHandler.dtoBase).appendDto(result))
        onResultUpdated?.invoke(sequenceHandler.finalResult)
        return result
    }

    suspend fun SequenceContext<DTO, DATA, ENTITY>.pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>
    = subTask("Pick", qualifiedName) { handler ->
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun SequenceContext<DTO, DATA, ENTITY>.pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
    = subTask("PickById", qualifiedName) { handler ->
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(conditions: SimpleQuery):ResultList<DTO, DATA, ENTITY>
    = subTask("Select", qualifiedName) { handler ->
       val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select():ResultList<DTO, DATA, ENTITY>
    = subTask("Select", qualifiedName) { handler ->
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModels: List<DATA>):ResultList<DTO, DATA, ENTITY>
    = subTask("Update(List)", qualifiedName) { handler ->
        val result = executionContext.update(dataModels)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>
            = subTask("Update(Single)", qualifiedName) { handler ->
        val result = executionContext.update(dataModel)
        submitLatestResult(result)
    }.resultOrException()

}


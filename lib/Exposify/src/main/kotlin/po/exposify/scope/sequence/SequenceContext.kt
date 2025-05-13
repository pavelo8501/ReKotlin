package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.Query
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask


class SequenceContext<DTO, DATA, ENTITY>(
   internal val handler: SequenceHandlerBase<DTO, DATA, ENTITY>,
   private val executionContext: ExecutionContext<DTO, DATA, ENTITY>
): TasksManaged, IdentifiableComponent where  DTO : ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val qualifiedName: String get() = "SequenceContext[${executionContext.providerName}]"
    override val name: String  get() = "SequenceContext"

    internal var onResultUpdated : ((ResultList<DTO, DATA, ENTITY>)-> Unit)?  = null
    private var latestResult : ResultList<DTO,DATA, ENTITY> = ResultList()
    internal fun submitLatestResult(result :  ResultList<DTO, DATA, ENTITY>):ResultList<DTO, DATA, ENTITY>{
       latestResult = result
       onResultUpdated?.invoke(result)
       return latestResult
    }
    internal fun submitLatestResult(result :  ResultSingle<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY>{
        latestResult = ResultList<DTO, DATA, ENTITY>().appendDto(result)
        onResultUpdated?.invoke(latestResult)
        return result
    }

    suspend fun pick(conditions: Query): ResultSingle<DTO, DATA, ENTITY>
    = subTask("Pick", qualifiedName) { handler ->
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun <T: IdTable<Long>> pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
    = subTask("PickById", qualifiedName) { handler ->
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(conditions: Query):ResultList<DTO, DATA, ENTITY>
    = subTask("Select", qualifiedName) { handler ->
       val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(
    ):ResultList<DTO, DATA, ENTITY>
    = subTask("Select", qualifiedName) { handler ->
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(
        dataModels: List<DATA>
    ):ResultList<DTO, DATA, ENTITY>
    = subTask("Update", qualifiedName) { handler ->
        val result = executionContext.update(dataModels)
        submitLatestResult(result)
    }.resultOrException()

}


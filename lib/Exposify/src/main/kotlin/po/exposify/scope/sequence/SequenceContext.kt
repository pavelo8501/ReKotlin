package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.extensions.checkDataListNotEmpty
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.exceptions.CoroutineInfo
import kotlin.coroutines.coroutineContext


class SequenceContext<DTO, DATA, ENTITY>(
   internal val sequenceHandler: SequenceHandlerBase<DTO, DATA, ENTITY>,
   private val executionContext: ExecutionContext<DTO, DATA, ENTITY>
): TasksManaged, IdentifiableComponent where  DTO : ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val qualifiedName: String get() = "SequenceContext[${executionContext.qualifiedName}]"
    override val type: ComponentType = ComponentType.SequenceContext
    private var latestSingleResult : ResultSingle<DTO,DATA, ENTITY> = ResultSingle(sequenceHandler.dtoBase)

    private var firstRun = true
    private suspend fun onFirsRun(){
        if(firstRun){
            val coroutineInfo = CoroutineInfo.createInfo(coroutineContext)
            val runnableContext =  RunnableContext.createRunInfo(coroutineContext[AuthorizedSession],coroutineInfo)
            sequenceHandler.handlerConfig.onStartCallback?.invoke(runnableContext)
            firstRun = false
        }
    }

    private fun submitLatestResult(result :  ResultList<DTO, DATA, ENTITY>):ResultList<DTO, DATA, ENTITY>{
       sequenceHandler.provideFinalResult(result)
       return result
    }
    private fun submitLatestResult(result :  ResultSingle<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY>{
        latestSingleResult = result
        sequenceHandler.provideFinalResult(ResultList(sequenceHandler.dtoBase).appendDto(result))
        return result
    }

    suspend fun SequenceContext<DTO, DATA, ENTITY>.pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>
    = subTask("Pick") { handler ->
        onFirsRun()
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun SequenceContext<DTO, DATA, ENTITY>.pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>
    = subTask("PickById") { handler ->
        onFirsRun()
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(conditions: SimpleQuery):ResultList<DTO, DATA, ENTITY>
    = subTask("Select") { handler ->
        onFirsRun()
        val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select():ResultList<DTO, DATA, ENTITY>
    = subTask("Select") { handler ->
        onFirsRun()
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModels: List<DATA>):ResultList<DTO, DATA, ENTITY>
    = subTask("Update(List)") { handler ->
        checkDataListNotEmpty(dataModels)
        onFirsRun()
        val result = executionContext.update(dataModels)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>
            = subTask("Update(Single)") { handler ->
        onFirsRun()
        val result = executionContext.update(dataModel)
        submitLatestResult(result)
    }.resultOrException()

}


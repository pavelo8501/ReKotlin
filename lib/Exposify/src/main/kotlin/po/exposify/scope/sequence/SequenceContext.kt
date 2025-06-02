package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.dto.models.ModuleType
import po.exposify.extensions.checkDataListNotEmpty
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.exceptions.CoroutineInfo
import po.misc.interfaces.IdentifiableModule
import kotlin.coroutines.coroutineContext


class SequenceContext<DTO, D, E>(
    internal val sequenceHandler: SequenceHandlerBase<DTO, D, E>,
    private val executionContext: ExecutionContext<DTO, D, E>,
    override val session : AuthorizedSession? = null,
    val moduleType: ModuleType = ModuleType.SequenceContext
):IdentifiableModule by moduleType ,  TasksManaged,  RunnableContext where  DTO : ModelDTO, D : DataModel, E: LongEntity
{
    override val coroutineInfo: CoroutineInfo
        get() =  CoroutineInfo.createInfo(session?.sessionScope()?.coroutineContext)
    private var latestSingleResult : ResultSingle<DTO,D, E> = ResultSingle(sequenceHandler.dtoBase)

    private var firstRun = true
    private suspend fun onFirsRun(){
        if(firstRun){
            val coroutineInfo = CoroutineInfo.createInfo(coroutineContext)
            val runnableContext =  RunnableContext.createRunInfo(coroutineContext[AuthorizedSession],coroutineInfo)
            sequenceHandler.handlerConfig.onStartCallback?.invoke(runnableContext)
            firstRun = false
        }
    }

    private fun submitLatestResult(result :  ResultList<DTO, D, E>):ResultList<DTO, D, E>{
       sequenceHandler.provideFinalResult(result)
       return result
    }
    private fun submitLatestResult(result :  ResultSingle<DTO, D, E>): ResultSingle<DTO, D, E>{
        latestSingleResult = result
        sequenceHandler.provideFinalResult(result)
        return result
    }

    suspend fun SequenceContext<DTO, D, E>.pick(conditions: SimpleQuery): ResultSingle<DTO, D, E>
    = subTask("Pick") {
        onFirsRun()
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun SequenceContext<DTO, D, E>.pickById(id: Long): ResultSingle<DTO, D, E>
    = subTask("PickById") {
        onFirsRun()
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select(conditions: SimpleQuery):ResultList<DTO, D, E>
    = subTask("Select") {
        onFirsRun()
        val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun select():ResultList<DTO, D, E>
    = subTask("Select") {
        onFirsRun()
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModels: List<D>):ResultList<DTO, D, E>
        = subTask("Update(List)") {
        checkDataListNotEmpty(dataModels)
        onFirsRun()
        val result = executionContext.update(dataModels)
        submitLatestResult(result)
    }.resultOrException()

    suspend fun update(dataModel: D): ResultSingle<DTO, D, E>
            = subTask("Update(Single)") {
        onFirsRun()
        val result = executionContext.update(dataModel)
        submitLatestResult(result)
    }.resultOrException()

}


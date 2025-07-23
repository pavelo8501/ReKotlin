package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.common.events.ContextData
import po.exposify.dto.components.ExecutionContext
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SequenceRunInfo
import po.exposify.extensions.checkDataListNotEmpty
import po.lognotify.TasksManaged
import po.lognotify.debug.debugProxy
import po.lognotify.extensions.runTask
import po.misc.context.CTX
import po.misc.context.asIdentity


class SequenceContext<DTO, D, E>(
    private val executionContext: ExecutionContext<DTO, D, E>,
    val runInfo : SequenceRunInfo
):TasksManaged, CTX where  DTO: ModelDTO, D: DataModel, E: LongEntity {

    override val identity = asIdentity()

  //  private var latestSingleResult : ResultSingle<DTO,D, E> = ResultSingle(sequenceHandler.dtoBase)

    private var firstRun = true

    internal val debug = debugProxy(this, ContextData, ContextData.Debug){
        ContextData(it.message)
    }

    private fun onFirsRun(){
        if(firstRun){
         //  sequenceHandler.handlerConfig.onStartCallback?.invoke(runInfo)
            firstRun = false
        }
    }

    private fun submitLatestResult(result :  ResultList<DTO, D, E>):ResultList<DTO, D, E>{
     //  sequenceHandler.provideFinalResult(result)
       return result
    }
    private fun submitLatestResult(result :  ResultSingle<DTO, D, E>): ResultSingle<DTO, D, E>{
     //   latestSingleResult = result
      //  sequenceHandler.provideFinalResult(result)
        return result
    }

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, D, E> =
        runTask("Pick") {
        onFirsRun()
        val result = executionContext.pick(conditions)
        submitLatestResult(result)
    }.resultOrException()

    fun pickById(id: Long): ResultSingle<DTO, D, E> =
        runTask("PickById") {
        onFirsRun()
        val result = executionContext.pickById(id)
        submitLatestResult(result)
    }.resultOrException()

    fun select(conditions: SimpleQuery):ResultList<DTO, D, E> =
        runTask("Select") {
        onFirsRun()
        val result = executionContext.select(conditions)
        submitLatestResult(result)
    }.resultOrException()

    fun select():ResultList<DTO, D, E> =
        runTask("Select") {
        onFirsRun()
        val result = executionContext.select()
        submitLatestResult(result)
    }.resultOrException()

}


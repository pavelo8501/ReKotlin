package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.builder.PickByIdChunk
import po.exposify.scope.sequence.builder.SelectChunk
import po.exposify.scope.sequence.builder.UpdateChunk
import po.exposify.scope.sequence.builder.UpdateListChunk
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.containers.DeferredContainer



private suspend fun <DTO, D> launchExecutionList(
    launchDescriptor: ListDescriptor<DTO, D>,
    session: AuthorizedSession,
    parameter: Long? = null,
    inputData:List<D>? = null,
    query: DeferredContainer<WhereQuery<*>>? = null
): ResultList<DTO, D> where DTO: ModelDTO, D:DataModel
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"

    val errorFallback = ExceptionFallback{
        val noContainerMsg = "No predefined execution for $launchDescriptor"
        OperationsException(noContainerMsg, ExceptionCode.Sequence_Setup_Failure, launchDescriptor)
    }
    val container =  launchDescriptor.chunksContainerBacking.getWithFallback(errorFallback)
    val service =  launchDescriptor.dtoClass.serviceClass

    val emitter = service.requestEmitter(session)

    return emitter.dispatchList {
        var effectiveResult: ResultList<DTO, D>? = null
        withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {

            if(query != null){
                container.listTypeHandler.provideWhereQuery(query)
            }

            container.listResultChunks.forEach {chunk ->
                when(chunk){
                    is SelectChunk<*, *> -> {
                        val selectChunk = chunk.castOrOperations<SelectChunk<DTO, D>>(launchDescriptor)
                        println("Chunk returned after being persisted in RootDTO")
                        selectChunk.healthMonitor.print()
                        effectiveResult = selectChunk.computeResult().castOrOperations(launchDescriptor)
                    }

                    is UpdateListChunk<*, *> -> {
                        val updateListChunk = chunk.castOrOperations<UpdateListChunk<DTO, D>>(launchDescriptor)
                        println("Chunk returned after being persisted in RootDTO")
                        updateListChunk.healthMonitor.print()
                        effectiveResult = updateListChunk.computeResult().castOrOperations(launchDescriptor)
                    }
                }
                chunk.healthMonitor.print()
            }
        }
        effectiveResult.getOrOperations(launchDescriptor)
    }
}



suspend fun <DTO, D> AuthorizedSession.launch(
    launchDescriptor: ListDescriptor<DTO, D>,
    deferredQuery: DeferredContainer<WhereQuery<*>>,
): ResultList<DTO, D> where DTO : ModelDTO, D : DataModel{
    return launchExecutionList(launchDescriptor, this,  query = deferredQuery)
}

suspend fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.launch(
    launchDescriptor: ListDescriptor<DTO, D>,
): ResultList<DTO, D> = launchExecutionList(launchDescriptor, this)



private suspend fun <DTO, D> launchExecutionSingle(
    launchDescriptor: SingleDescriptor<DTO, D>,
    session: AuthorizedSession,
    parameter: Long? = null,
    inputData:D? = null,
    query: DeferredContainer<WhereQuery<*>>? = null,
): ResultSingle<DTO, D> where DTO : ModelDTO, D : DataModel
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"

    val errorFallback = ExceptionFallback{
        val noContainerMsg = "No predefined execution for $launchDescriptor"
        OperationsException(noContainerMsg, ExceptionCode.Sequence_Setup_Failure, launchDescriptor)
    }

    val container =  launchDescriptor.chunksContainerBacking.getWithFallback(errorFallback)
    val service =  launchDescriptor.dtoClass.serviceClass


    val emitter = service.requestEmitter(session)
    return emitter.dispatchSingle {
        var effectiveResult: ResultSingle<DTO, D>? = null

        if(parameter != null){
            val deferredParameter = DeferredContainer<Long>(launchDescriptor){ parameter }
            container.singleTypeHandler.provideDeferredParameter(deferredParameter)
        }
        if(inputData != null){
            val deferredInput = DeferredContainer<D>(launchDescriptor){ inputData }
            container.singleTypeHandler.provideDeferredInput(deferredInput)
        }

        if(query != null){
            container.singleTypeHandler.provideWhereQuery(query)
        }
        container.singleResultChunks.forEach { chunk ->
            when(chunk){
                is PickByIdChunk <*, *>-> {
                    val pickById = chunk.castOrOperations<PickByIdChunk<DTO, D>>(launchDescriptor)
                    println("Chunk returned after being persisted in RootDTO")
                    pickById.healthMonitor.print()
                    effectiveResult = pickById.computeResult().castOrOperations(launchDescriptor)
                }

                is UpdateChunk<*, *> -> {
                    val updateChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(launchDescriptor)
                    println("Chunk returned after being persisted in RootDTO")
                    updateChunk.healthMonitor.print()
                    effectiveResult = updateChunk.computeResult().castOrOperations(launchDescriptor)
                }
            }
        }
        effectiveResult.getOrOperations(launchDescriptor)
    }
}

suspend fun <DTO: ModelDTO, D: DataModel> AuthorizedSession.launch(
    launchDescriptor: SingleDescriptor<DTO, D>,
    parameter: Long
): ResultSingle<DTO, D> = launchExecutionSingle(launchDescriptor, this, parameter = parameter)

suspend fun <DTO: ModelDTO, D: DataModel>  AuthorizedSession.launch(
    launchDescriptor: SingleDescriptor<DTO, D>,
    inputData: D,
): ResultSingle<DTO, D> = launchExecutionSingle(launchDescriptor, this,  inputData = inputData)

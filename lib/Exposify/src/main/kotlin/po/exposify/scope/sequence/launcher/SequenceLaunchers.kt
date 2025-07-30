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



private suspend fun <DTO, D, E> launchExecutionList(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    session: AuthorizedSession,
    parameter: Long? = null,
    inputData:List<D>? = null,
    query: DeferredContainer<WhereQuery<E>>? = null
): ResultList<DTO, D, E> where DTO: ModelDTO, D:DataModel, E: LongEntity
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
        var effectiveResult: ResultList<DTO, D, E>? = null
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



suspend fun <DTO, D, E> AuthorizedSession.launch(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    deferredQuery: DeferredContainer<WhereQuery<E>>,
): ResultList<DTO, D, *> where DTO : ModelDTO, D : DataModel, E: LongEntity {
    return launchExecutionList(launchDescriptor, this,  query = deferredQuery)
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.launch(
    launchDescriptor: ListDescriptor<DTO, D, E>,
): ResultList<DTO, D, *> = launchExecutionList(launchDescriptor, this)



private suspend fun <DTO, D, E> launchExecutionSingle(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    session: AuthorizedSession,
    parameter: Long? = null,
    inputData:D? = null,
    query: DeferredContainer<WhereQuery<E>>? = null,
): ResultSingle<DTO, D, E> where DTO : ModelDTO, D : DataModel, E : LongEntity
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
        var effectiveResult: ResultSingle<DTO, D, E>? = null

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

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.launch(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    parameter: Long
): ResultSingle<DTO, D, *> = launchExecutionSingle(launchDescriptor, this, parameter = parameter)

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.launch(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    inputData: D,
): ResultSingle<DTO, D, *> = launchExecutionSingle(launchDescriptor, this,  inputData = inputData)

package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.builder.PickByIdChunk
import po.exposify.scope.sequence.builder.SelectChunk
import po.exposify.scope.sequence.builder.UpdateChunk
import po.exposify.scope.sequence.builder.UpdateListChunk
import po.misc.functions.containers.DeferredContainer

private suspend fun <DTO, D, E> launchExecutionSingle(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    parameter: Long? = null,
    inputData:D? = null,
    query: DeferredContainer<WhereQuery<E>>? = null,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO : ModelDTO, D : DataModel, E : LongEntity
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"
    val container =  launchDescriptor.container
    val service =  launchDescriptor.dtoBaseClass.serviceClass
    val emitter = service.requestEmitter(session)
   return emitter.dispatchSingle {
       var effectiveResult: ResultSingle<DTO, D, *>? = null

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
                   effectiveResult = pickById.computeResult()
               }

               is UpdateChunk<*, *> -> {
                   val updateChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(launchDescriptor)
                   println("Chunk returned after being persisted in RootDTO")
                   updateChunk.healthMonitor.print()
                   effectiveResult = updateChunk.computeResult()
               }
           }
       }
       effectiveResult.getOrOperations("effectiveResult")
    }
}

private suspend fun <DTO, D, E> launchExecutionList(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    parameter: Long? = null,
    inputData:List<D>? = null,
    query: DeferredContainer<WhereQuery<E>>? = null,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO: ModelDTO, D:DataModel, E: LongEntity
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"
    val container =  launchDescriptor.container
    val service =  launchDescriptor.dtoBaseClass.serviceClass
    val emitter = service.requestEmitter(session)

    return emitter.dispatchList {
        var effectiveResult: ResultList<DTO, D, *>? = null
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
                        effectiveResult = selectChunk.computeResult()
                    }

                    is UpdateListChunk<*, *> -> {
                        val updateListChunk = chunk.castOrOperations<UpdateListChunk<DTO, D>>(launchDescriptor)
                        println("Chunk returned after being persisted in RootDTO")
                        updateListChunk.healthMonitor.print()
                        effectiveResult = updateListChunk.computeResult()
                    }
                }
                chunk.healthMonitor.print()
            }
        }
        effectiveResult.getOrOperations("effectiveResult")
    }
}


suspend fun <DTO, D, E>  AuthorizedSession.launch(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    parameter: Long
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel, E : LongEntity{
   return launchExecutionSingle<DTO,  D, E>(launchDescriptor =  launchDescriptor, parameter = parameter, session =  this)
}

suspend fun <DTO, D, E> launch(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    parameter: Long,
    session:AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel, E : LongEntity{
    return launchExecutionSingle(launchDescriptor = launchDescriptor, parameter =  parameter, session =  session)
}


//suspend fun <DTO, D, E>  AuthorizedSession.launch(
//    launchDescriptor: ParametrizedSingeDescriptor<DTO, D, E>,
//    parameter: D
//): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel, E : LongEntity{
//    return launchExecutionSingle(launchDescriptor, parameter, this)
//}
//
//suspend fun <DTO, D, E>  launch(
//    launchDescriptor: ParametrizedSingeDescriptor<DTO, D, E>,
//    parameter: D,
//    session: AuthorizedSession
//): ResultSingle<DTO, D, *> where DTO : ModelDTO, D : DataModel, E : LongEntity{
//    return launchExecutionSingle(launchDescriptor, parameter, session)
//}

suspend fun <DTO, D, E> launch(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO : ModelDTO, D : DataModel, E : LongEntity{
    return launchExecutionList(launchDescriptor, session = session)
}

suspend fun <DTO, D, E> launch(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    deferredQuery: DeferredContainer<WhereQuery<E>>,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO : ModelDTO, D : DataModel, E: LongEntity {
    return launchExecutionList(launchDescriptor, query = deferredQuery,  session =  session)
}
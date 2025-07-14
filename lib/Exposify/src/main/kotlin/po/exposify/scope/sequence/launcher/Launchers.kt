package po.exposify.scope.sequence.launcher

import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.builder.InsertChunk
import po.exposify.scope.sequence.builder.PickByIdChunk
import po.exposify.scope.sequence.builder.SequenceChunkContainer


private fun <DTO, D, P: Any> launchExecutionResultSingle(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    parameter:P,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel
{
    val container =  launchDescriptor.container
    var activeResult: ResultSingle<DTO, D, *>? = null
    withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {
        container.chunks.forEach { chunk ->

            when(chunk){
                is PickByIdChunk ->  {
                    val pickByIdChunk =  chunk
                    println("Chunk returned after being persisted in RootDTO")
                    pickByIdChunk.healthMonitor.print()
                    if(parameter is Long){
                        pickByIdChunk.inputContainer.registerProvider { parameter }
                    }else{
                       throw operationsException("Input parameter type does not match chunk processed type", ExceptionCode.ABNORMAL_STATE)
                    }
                }
                is InsertChunk -> chunk
            }
            activeResult = chunk.computeResult().castOrOperations<ResultSingle<DTO, D, *>>(launchDescriptor.dtoClass)

            chunk.healthMonitor.print()
        }
    }
    return activeResult.getOrOperations("activeResult")
}


fun <DTO, D>  AuthorizedSession.launch(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    parameter: Long
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{

   return launchExecutionResultSingle(launchDescriptor, parameter, this)
}

fun <DTO, D> launch(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    parameter: Long,
    session:AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{
    return launchExecutionResultSingle(launchDescriptor, parameter, session)
}



//fun <DTO, D> launch(
//    launchDescriptor: IdLaunchDescriptor<DTO, D>,
//    parameter: Long
//): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{
//
//    val container = launchDescriptor.container
//    var activeResult: ResultBase<DTO, D>? = null
//    withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {
//        container.chunks.forEach { chunk ->
//            val castedChunk = chunk.castOrOperations<PickByIdChunk<DTO, D>>(launchDescriptor.dtoClass)
//            println("Chunk returned after being persisted in RootDTO")
//            castedChunk.healthMonitor.print()
//            castedChunk.inputContainer.registerProvider { parameter }
//            val result =  castedChunk.computeResult()
//            activeResult = result
//            castedChunk.healthMonitor.print()
//        }
//    }
//    return activeResult.castOrOperations<ResultSingle<DTO, D, *>>(launchDescriptor.dtoClass)
//}

fun <DTO, D, P> launch(
    launchDescriptor: ParametrizedSinge<DTO, D, P>,
    parameter:P
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel, P:D {

    val container = launchDescriptor.container
    val dtoClass = container.execContext.dtoClass
    var activeResult: ResultBase<DTO, D>? = null

    withTransactionIfNone(container.debugger,  warnIfNoTransaction = false) {
        container.chunks.forEach { chunk ->
            val castedChunk = chunk.castOrOperations<InsertChunk<DTO, D, D>>(launchDescriptor.dtoClass)
            println("Chunk returned after being persisted in RootDTO")
            castedChunk.healthMonitor.print()
            castedChunk.inputContainer.registerProvider { parameter }
            val result =  castedChunk.computeResult()
            activeResult = result
            castedChunk.healthMonitor.print()
        }
    }
    return activeResult.castOrOperations<ResultSingle<DTO, D, *>>(dtoClass)
}
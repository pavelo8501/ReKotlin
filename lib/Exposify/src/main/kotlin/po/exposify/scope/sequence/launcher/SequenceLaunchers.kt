package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.LongIdTable
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
import po.misc.functions.containers.DeferredContainer


private fun <DTO, D> launchExecutionSingle(
    launchDescriptor: LongSingeDescriptor<DTO, D>,
    parameter: Long,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"

    val container =  launchDescriptor.container
    var activeResult: ResultSingle<DTO, D, *>? = null
    withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {
        container.chunks.forEach { chunk ->
            when(chunk){
                is PickByIdChunk ->  {
                    val pickByIdChunk =  chunk
                    println("Chunk returned after being persisted in RootDTO")
                    pickByIdChunk.healthMonitor.print()
                    pickByIdChunk.inputContainer.registerProvider { parameter }
                    activeResult = chunk.computeResult()
                }
                else -> throw operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
            }
            chunk.healthMonitor.print()
        }
    }
    return activeResult.getOrOperations("activeResult")
}

private fun <DTO, D> launchExecutionSingle(
    launchDescriptor: ParametrizedSingeDescriptor<DTO, D>,
    parameter:D,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"
    val container =  launchDescriptor.container
    var activeResult: ResultSingle<DTO, D, *>? = null
    withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {
        container.chunks.forEach { chunk ->
            when(chunk){
                is UpdateChunk<*, *> -> {
                    val insertChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(launchDescriptor)
                    println("Chunk returned after being persisted in RootDTO")
                    insertChunk.healthMonitor.print()
                    insertChunk.inputContainer.registerProvider { parameter }
                    activeResult = insertChunk.computeResult()
                }
                else -> throw operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
            }
            chunk.healthMonitor.print()
        }
    }
    return activeResult.getOrOperations("activeResult")
}



fun <DTO, D>  AuthorizedSession.launch(
    launchDescriptor: LongSingeDescriptor<DTO, D>,
    parameter: Long
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{
   return launchExecutionSingle(launchDescriptor, parameter, this)
}

fun <DTO, D> launch(
    launchDescriptor: LongSingeDescriptor<DTO, D>,
    parameter: Long,
    session:AuthorizedSession
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{
    return launchExecutionSingle(launchDescriptor, parameter, session)
}


fun <DTO, D>  AuthorizedSession.launch(
    launchDescriptor: ParametrizedSingeDescriptor<DTO, D>,
    parameter: D
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel{
    return launchExecutionSingle(launchDescriptor, parameter, this)
}

fun <DTO, D>  launch(
    launchDescriptor: ParametrizedSingeDescriptor<DTO, D>,
    parameter: D,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO : ModelDTO, D : DataModel{
    return launchExecutionSingle(launchDescriptor, parameter, session)
}



private suspend fun <DTO, D, P> launchExecutionList(
    launchDescriptor: ListDescriptor<DTO, D>,
    parameter:P,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO: ModelDTO, D:DataModel, P:Any
{
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"
    val container =  launchDescriptor.container
    val service =  launchDescriptor.dtoBaseClass.serviceClass
    val emitter = service.requestEmitter(session)

    return emitter.dispatch {
        var effectiveResult: ResultList<DTO, D, *>? = null
        withTransactionIfNone(container.debugger, warnIfNoTransaction = false) {
            container.chunks.forEach { chunk ->
                when(chunk){
                    is SelectChunk<*, *> -> {
                        val insertChunk = chunk.castOrOperations<SelectChunk<DTO, D>>(launchDescriptor)
                        println("Chunk returned after being persisted in RootDTO")
                        insertChunk.healthMonitor.print()
                        effectiveResult = insertChunk.computeResult()
                    }
                    else -> throw operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
                }
                chunk.healthMonitor.print()
            }
        }
        effectiveResult.getOrOperations("effectiveResult")
    }
}

suspend fun <DTO, D> launch(
    launchDescriptor: ListDescriptor<DTO, D>,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO : ModelDTO, D : DataModel{
    return launchExecutionList(launchDescriptor, Unit, session)
}

suspend fun <DTO, D, E> launch(
    launchDescriptor: ListDescriptor<DTO, D>,
    deferredQuery: DeferredContainer<WhereQuery<E>>,
    session: AuthorizedSession
): ResultList<DTO, D, *> where DTO : ModelDTO, D : DataModel, E: LongEntity {
    return launchExecutionList(launchDescriptor, Unit, session)
}
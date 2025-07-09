package po.exposify.scope.sequence.launcher

import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.builder.ExecutionChunkBase
import po.misc.functions.containers.PromiseResultContainer
import po.misc.interfaces.CtxId


fun <DTO, D> launch(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    parameter: Long
) where DTO: ModelDTO, D:DataModel{

}

fun <DTO, D, P> launch(
    launchDescriptor: ParametrizedSinge<DTO, D, P>,
    parameter:P
): ResultSingle<DTO, D, *> where DTO: ModelDTO, D:DataModel, P:D {

    fun initializePromise(value: P, container: PromiseResultContainer<P>) {
        container.setResultProvider {
            value
        }
    }
    fun arrangeSubscriptions(chunk: ExecutionChunkBase<DTO, D, P>, subscribingContext: CtxId) {
        chunk.onInitialized.subscribe(subscribingContext) {
            val initializedChunk = it.getData()
            subscribingContext.echo("$initializedChunk OnInitialized") {
                printLine()
            }
        }
    }
    val container = launchDescriptor.container
    val dtoClass = container.dtoClass
    var activeResult: ResultBase<DTO, D>? = null

    withTransactionIfNone(container.debugger, false) {
        container.chunks.forEach { chunk ->
            val container = chunk.inputContainer
            println("Chunk returned after being persisted in RootDTO")
            chunk.healthMonitor.print()
            val casted = container.castOrOperations<PromiseResultContainer<P>>(chunk.inputContainer.ctx)
            initializePromise(parameter, casted)
            chunk.computeResult()
            chunk.healthMonitor.print()
            activeResult = chunk.returnResult()
        }
    }
    return activeResult.castOrOperations<ResultSingle<DTO, D, *>>(dtoClass)
}
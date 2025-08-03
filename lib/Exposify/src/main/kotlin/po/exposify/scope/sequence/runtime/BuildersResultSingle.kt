package po.exposify.scope.sequence.runtime

import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.PickChunk
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleResultChunks
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.UpdateChunk
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.misc.functions.containers.DeferredContainer

fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultSingle<DTO, D>> where DTO : ModelDTO, D : DataModel {
    val pickByIdChunk = PickChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)

    pickByIdChunk.subscribeParameter { parameter ->
        val result = descriptor.dtoClass.executionContext.pickById(parameter)
        println("Chunk $pickByIdChunk Initiated by parameter: $parameter")
        println("Result received $result")
        result
    }

    pickByIdChunk.subscribeQuery { queryContainer ->
        val query = queryContainer.resolve()
        val result = descriptor.dtoClass.executionContext.pick(query)
        println("Chunk $pickByIdChunk Initiated by query: $query")
        println("Result received $result")
        result
    }
    registerChunk(pickByIdChunk)
    return pickByIdChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.update(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultSingle<DTO, D>> where DTO : ModelDTO, D : DataModel {
    val updateChunk = UpdateChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)
    updateChunk.subscribeData { dataInput ->
        val result = descriptor.dtoClass.executionContext.update(dataInput, updateChunk)
        println("Chunk $updateChunk Initiated by data: $dataInput")
        println("Result received $result")
        result
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultSingle<DTO, D>> where DTO : ModelDTO, D : DataModel = update(handler, configurationBlock)





fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.update(
    handler: SingleTypeSwitchHandler<DTO, D, F, FD>,
    configurationBlock: SingleResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultSingle<DTO, D>>
        where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {

    val updateChunk = UpdateChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}

package po.exposify.scope.sequence.runtime

import po.exposify.dto.components.result.ResultList
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.ListResultChunks
import po.exposify.scope.sequence.builder.SelectChunk
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.UpdateListChunk
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.ListTypeSwitchHandler
import po.misc.functions.containers.DeferredContainer



fun <DTO, D> SequenceChunkContainer<DTO, D>.update(
    handler: ListTypeHandler<DTO, D>,
    configurationBlock: ListResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultList<DTO, D>> where DTO : ModelDTO, D : DataModel {

    val updateListChunk = UpdateListChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)

    registerChunk(updateListChunk)
    return updateListChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.select(
    handler: ListTypeHandler<DTO, D>,
    configurationBlock: ListResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultList<DTO, D>> where DTO : ModelDTO, D : DataModel {
    val selectChunk = SelectChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)

    registerChunk(selectChunk)
    return selectChunk.resultContainer
}

fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.select(
    handler: ListTypeSwitchHandler<DTO, D, F, FD>,
    configurationBlock: ListResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultList<DTO, D>>
        where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel
{
    val selectChunk = SelectChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)
    registerChunk(selectChunk)
    return selectChunk.resultContainer
}


fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.update(
    handler: ListTypeSwitchHandler<DTO, D, F, FD>,
    configurationBlock: ListResultChunks<DTO, D>.() -> Unit,
): DeferredContainer<ResultList<DTO, D>>
        where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    val updateListChunk = UpdateListChunk(handler.descriptor.dtoClass.commonDTOType, configurationBlock)
    registerChunk(updateListChunk)
    return updateListChunk.resultContainer
}

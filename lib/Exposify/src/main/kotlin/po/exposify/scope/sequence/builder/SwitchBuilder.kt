package po.exposify.scope.sequence.builder

import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListTypeSwitchHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.misc.functions.containers.DeferredContainer



fun <DTO, D, F, FD> ChunkContainer<F, FD>.switchStatement(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(SingleTypeSwitchHandler<DTO, D, F, FD>) -> DeferredContainer<ResultSingle<DTO, D>>,
) where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
    val switchChunksContainer: SwitchChunkContainer<DTO, D, F, FD> = SwitchChunkContainer(switchDescriptor, this)

    block.invoke(switchChunksContainer, switchChunksContainer.singleHandler)
    switchChunksContainer.chunks.forEach { chunk ->
        chunk.configure()
    }
    switchDescriptor.registerChunkContainer(switchChunksContainer)
}

fun <DTO, D, F, FD> ChunkContainer<F, FD>.switchDTO(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(SingleTypeSwitchHandler<DTO, D, F, FD>) -> DeferredContainer<ResultSingle<DTO, D>>,
): Unit where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel = switchStatement(switchDescriptor,  block)


fun <DTO, D, F, FD> ChunkContainer<F, FD>.switchDTO(
    switchDescriptor: SwitchListDescriptor<DTO, D, F, FD>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(ListTypeSwitchHandler<DTO, D, F, FD>) -> DeferredContainer<ResultList<DTO, D>>,
): Unit where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel{

    val switchChunksContainer: SwitchChunkContainer<DTO, D, F, FD> = SwitchChunkContainer(switchDescriptor, this)
    block.invoke(switchChunksContainer, switchChunksContainer.listHandler)
    switchChunksContainer.chunks.forEach { chunk ->
        chunk.configure()
    }
    switchDescriptor.registerChunkContainer(switchChunksContainer)
}
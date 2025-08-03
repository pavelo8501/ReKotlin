package po.exposify.scope.sequence.builder

import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.DeferredContainer

fun <DTO, D> ChunkBase<DTO, D, ResultBase<DTO, D, *>>.withResult(
    block: ResultBase<DTO, D, *>.() -> Unit,
) where DTO : ModelDTO, D : DataModel {
//    when(this){
//        is SingleResultChunks->{
//            withResultContainer.registerProvider(block)
//        }
//        is ListResultChunks->{
//            withResultContainer.registerProvider(block)
//        }
//    }
}

// fun <DTO, D> ChunkBase<DTO, D, CommonInputType<*>, ResultBase<DTO, D, *>>.withInputValue(
//    block: D.() -> Unit,
// ) where DTO : ModelDTO, D : DataModel {
//    ListResultChunks.registerProvider(block)
// }



private fun <DTO, D> sequencedSingle(
    descriptor: RootDescriptorBase<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SingleTypeHandler<DTO, D>) -> DeferredContainer<ResultSingle<DTO, D>>,
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel {
    val chunkContainer = SequenceChunkContainer(descriptor)
    block.invoke(chunkContainer, chunkContainer.singleTypeHandler)
    chunkContainer.chunks.forEach { chunk ->
        chunk.configure()
    }
    return chunkContainer
}

private fun <DTO, D> sequencedList(
    descriptor: RootDescriptorBase<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(ListTypeHandler<DTO, D>) -> DeferredContainer<ResultList<DTO, D>>,
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel {
    val chunkContainer = SequenceChunkContainer(descriptor)
    block.invoke(chunkContainer, chunkContainer.listTypeHandler)
    chunkContainer.chunks.forEach { chunk ->
        chunk.configure()
    }
    return chunkContainer
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: SingleDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SingleTypeHandler<DTO, D>) -> DeferredContainer<ResultSingle<DTO, D>>,
) where DTO : ModelDTO, D : DataModel {
    val chunkContainer = sequencedSingle(launchDescriptor, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ListDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(ListTypeHandler<DTO, D>) -> DeferredContainer<ResultList<DTO, D>>,
) where DTO : ModelDTO, D : DataModel {
    val chunkContainer = sequencedList(launchDescriptor, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}

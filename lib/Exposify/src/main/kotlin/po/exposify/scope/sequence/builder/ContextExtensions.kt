package po.exposify.scope.sequence.builder

import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.IdLaunchDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSinge
import po.exposify.scope.sequence.models.SequenceParameter
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.DeferredInputContainer
import po.misc.functions.containers.LambdaContainer


fun <DTO: ModelDTO, D: DataModel, P: Any> ExecutionChunkBase<DTO, D, P>.deferredResult(): DeferredInputContainer<P, ResultBase<DTO, D>>{
    return resultContainer
}


fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withInputValue(
    block:P.()-> Unit
) where DTO : ModelDTO, D : DataModel, P:Any {

    withInputContainer.registerProvider(block)
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    inputContainer: LambdaContainer<D>,
    configurationBlock: ExecutionChunkBase<DTO, D, D>.()->  Unit
): DeferredInputContainer<D, ResultBase<DTO, D>> where DTO: ModelDTO, D: DataModel {

    val insertChunk = InsertChunk.create(configurationBlock)
    insertChunk.configContainer.registerProvider(configurationBlock)
    insertChunk.resultContainer.registerProvider{ data->
        val result = execContext.insert(data)
        result
    }
    registerChunk(insertChunk)
    return insertChunk.resultContainer
}


fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    paramContainer: LambdaContainer<Long>,
    configurationBlock: ExecutionChunkBase<DTO, D, Long>.()->  Unit
): DeferredInputContainer<Long, ResultBase<DTO, D>> where DTO: ModelDTO, D: DataModel {

    val pickByIdChunk = PickByIdChunk.create(configurationBlock)
    pickByIdChunk.configContainer.registerProvider(configurationBlock)

    pickByIdChunk.resultContainer.registerProvider{ id->
        val result = execContext.pickById(id)
        result
    }
    registerChunk(pickByIdChunk)
    return pickByIdChunk.resultContainer
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<Long>) -> DeferredInputContainer<Long, ResultBase<DTO, D>>
) where DTO : ModelDTO, D : DataModel {

    val execContext = dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 100)
    val container = LambdaContainer<Long>(dtoClass)
    val sequenceParameter = SequenceParameter<Long>(container)
    block.invoke(chunkContainer, sequenceParameter)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    launchDescriptor.registerChunkContainer(chunkContainer)
}


fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ParametrizedSinge<DTO, D, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<D>) -> DeferredInputContainer<D, ResultBase<DTO, D>>
) where DTO : ModelDTO, D : DataModel{

    val execContext = dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 100)
    val container = LambdaContainer<D>(this)
    val sequenceParameter = SequenceParameter<D>(container)
    block.invoke(chunkContainer, sequenceParameter)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    launchDescriptor.registerChunkContainer(chunkContainer)
}
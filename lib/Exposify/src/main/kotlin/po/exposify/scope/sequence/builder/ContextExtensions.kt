package po.exposify.scope.sequence.builder

import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.DTOResult
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.helpers.asCommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.LongSingeDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSingeDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSwitchSinge
import po.exposify.scope.sequence.launcher.SwitchSinge
import po.exposify.scope.sequence.models.SequenceParameter
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.LazyExecutionContainer


fun <DTO: ModelDTO, D: DataModel, P: Any> ExecutionChunkBase<DTO, D, P>.deferredResult(): LazyExecutionContainer<P, ResultBase<DTO, D>>{
    return resultContainer
}


fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withResult(
    block: DTOResult<DTO>.()-> Unit
) where DTO : ModelDTO, D : DataModel, P : Any {
    withResultContainer.registerProvider(block)
}

fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withInputValue(
    block:P.()-> Unit
) where DTO: ModelDTO, D: DataModel, P:Any {
    withInputContainer.registerProvider(block)
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    inputContainer: LambdaContainer<D>,
    configurationBlock: SingleResultChunks<DTO, D, D>.()->  Unit
): LazyExecutionContainer<D, ResultBase<DTO, D>> where DTO : ModelDTO, D : DataModel{

    val insertChunk = UpdateChunk.create(configurationBlock)
   // insertChunk.configContainer.registerProvider(configurationBlock)
    insertChunk.resultContainer.registerProvider{
        val result = execContext.insert(this)
        result
    }
    registerChunk(insertChunk)
    return insertChunk.resultContainer
}

fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.update(
    inputContainer: LambdaContainer<FD>,
    configurationBlock: SingleResultChunks<F, FD, FD>.()->  Unit
): LazyExecutionContainer<FD, ResultBase<F, FD>>
where DTO: ModelDTO, D: DataModel, F : ModelDTO, FD: DataModel{

    val updateChunk = UpdateChunk.create(configurationBlock)
    updateChunk.resultContainer.registerParametrizedProvider{ data ->
        val result = execContext.updateSingle(this, this@update)
        result
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}



fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    paramContainer: LambdaContainer<Long>,
    configurationBlock: SingleResultChunks<DTO, D, Long>.()->  Unit
): LazyExecutionContainer<Long, ResultBase<DTO, D>> where DTO: ModelDTO, D: DataModel
{
    val pickByIdChunk = PickByIdChunk.create(configurationBlock)
   // pickByIdChunk.configContainer.registerProvider(configurationBlock)

    pickByIdChunk.resultContainer.registerProvider{
        val result = execContext.pickById(this)
        result
    }
    registerChunk(pickByIdChunk)
    return pickByIdChunk.resultContainer
}



fun <DTO, D, F, FD> SingleResultChunks<DTO, D, *>.switchStatement(
    switchDescriptor: SwitchSinge<F, FD>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(SequenceParameter<Long>) -> LazyExecutionContainer<Long, ResultBase<F, FD>>
) where DTO : ModelDTO, D : DataModel, F: ModelDTO, FD: DataModel{

    activeResult?.let {result->
        val commonDTO =  result.dto.asCommonDTO(result.dtoClass)
        val switchContainer = createSwitchContainer(commonDTO, switchDescriptor.dtoClass)
        val container = LambdaContainer<Long>(result.dtoClass)
        val sequenceParameter = SequenceParameter<Long>(container)
        block.invoke(switchContainer, sequenceParameter)

    }
}

fun <DTO, D, F, FD> SingleResultChunks<DTO, D, *>.switchStatement(
    switchDescriptor: ParametrizedSwitchSinge<F, FD>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(SequenceParameter<FD>) -> LazyExecutionContainer<FD, ResultBase<F, FD>>
) where DTO : ModelDTO, D : DataModel, F: ModelDTO, FD: DataModel{

    resultContainer.hooks.onResolved {result->
        if(result is ResultSingle<DTO, D, *>){
            val commonDTO =  result.dto.asCommonDTO(result.dtoClass)
            val switchContainer = createSwitchContainer(commonDTO, switchDescriptor.dtoClass)
            val container = LambdaContainer<FD>(result.dtoClass)
            val sequenceParameter = SequenceParameter<FD>(container)
            block.invoke(switchContainer, sequenceParameter)
        }
    }
}


fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: LongSingeDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<Long>) -> LazyExecutionContainer<Long, ResultBase<DTO, D>>
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
    launchDescriptor: ParametrizedSingeDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<D>) -> LazyExecutionContainer<D, ResultBase<DTO, D>>
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
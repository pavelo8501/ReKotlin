package po.exposify.scope.sequence.builder

import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.helpers.asCommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListDescriptor
import po.exposify.scope.sequence.launcher.ListResultHandler
import po.exposify.scope.sequence.launcher.LongSingeDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSingeDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSwitchSinge
import po.exposify.scope.sequence.launcher.SwitchSinge
import po.exposify.scope.sequence.models.SequenceParameter
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.LazyExecutionContainer


fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withResult(
    block: ResultBase<DTO, D>.()-> Unit
) where DTO : ModelDTO, D : DataModel, P : Any {

    when(this){
        is SingleResultChunks->{
            withResultContainer.registerProvider(block)
        }
        is ListResultChunks->{
            withResultContainer.registerProvider(block)
        }
    }
}

fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withInputValue(
    block:P.()-> Unit
) where DTO: ModelDTO, D: DataModel, P:Any {
    withInputContainer.registerProvider(block)
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
    block: SwitchChunkContainer<DTO, D, F, FD>.(SequenceParameter<FD>) -> LazyExecutionContainer<FD, ResultSingle<F, FD, *>>
) where DTO : ModelDTO, D : DataModel, F: ModelDTO, FD: DataModel{

    resultContainer.hooks.onResolved {result->
        val commonDTO =  result.dto.asCommonDTO(result.dtoClass)
        val switchContainer = createSwitchContainer(commonDTO, switchDescriptor.dtoClass)
        val container = LambdaContainer<FD>(result.dtoClass)
        val sequenceParameter = SequenceParameter<FD>(container)
        block.invoke(switchContainer, sequenceParameter)
    }
}


private fun <DTO, D, P: Any>  sequencedSingle(
    serviceContext: ServiceContext<DTO, D, *>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<P>) -> LazyExecutionContainer<P, ResultSingle<DTO, D, *>>
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel  {

    val execContext = serviceContext.dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 100)
    val container = LambdaContainer<P>(serviceContext.dtoClass)
    val sequenceParameter = SequenceParameter<P>(container)
    block.invoke(chunkContainer, sequenceParameter)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: LongSingeDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<Long>) -> LazyExecutionContainer<Long, ResultSingle<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel {

    val chunkContainer = sequencedSingle(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ParametrizedSingeDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<D>) -> LazyExecutionContainer<D, ResultSingle<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel{
    val chunkContainer = sequencedSingle(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}


private fun <DTO, D, P: Any>  sequencedList(
    launchDescriptor: ListDescriptor<DTO, D>,
    serviceContext: ServiceContext<DTO, D, *>,
    block: SequenceChunkContainer<DTO, D>.(ListResultHandler<DTO, D>) -> LazyExecutionContainer<P, ResultList<DTO, D, *>>
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel  {

    val execContext = serviceContext.dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 300)
    //val container = LambdaContainer<P>(serviceContext.dtoClass)
    //val sequenceParameter = SequenceParameter<P>(container)

    val listResultHandler = ListResultHandler.create(launchDescriptor)

    block.invoke(chunkContainer,  listResultHandler)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}


fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ListDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(ListResultHandler<DTO, D>) -> LazyExecutionContainer<Unit, ResultList<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel
{
    val chunkContainer = sequencedList(launchDescriptor, this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}
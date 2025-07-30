package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations
import po.exposify.scope.sequence.launcher.ListDescriptor
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleDescriptor
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.exposify.scope.sequence.launcher.SwitchSingeDescriptor
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.DeferredContainer


fun <DTO, D> ExecutionChunkBase<DTO, D>.withResult(
    block: ResultBase<DTO, D, *>.()-> Unit
) where DTO : ModelDTO, D : DataModel{

    when(this){
        is SingleResultChunks->{
            withResultContainer.registerProvider(block)
        }
        is ListResultChunks->{
            withResultContainer.registerProvider(block)
        }
    }
}

fun <DTO, D> ExecutionChunkBase<DTO, D>.withInputValue(
    block:D.()-> Unit
) where DTO: ModelDTO, D: DataModel{
    withInputValueLambda.registerProvider(block)

}

fun <DTO, D, F, FD> SingleResultChunks<F, FD>.switchStatement(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F>,
    block: SwitchChunkContainer<DTO, D, F, FD>.(SingleTypeSwitchHandler<DTO, D, F, FD>) -> DeferredContainer<ResultSingle<DTO, D>>
) where DTO : ModelDTO, D : DataModel, F: ModelDTO, FD: DataModel{

    val consResult = activeResult
    if(consResult != null){
        consResult.getAsCommonDTO()?.let {foreignDTO->
            val castedForeign = foreignDTO.castOrOperations<CommonDTO<F, FD, *>>(switchDescriptor)
            val switchContainer = SwitchChunkContainer(switchDescriptor, castedForeign, this)
            block.invoke(switchContainer,  switchContainer.singleTypeSwitchHandler)
            registerSwitchContainer(switchContainer, switchDescriptor.inputType)
        }?:run { println("SwitchChunkContainer can not be created. Result is empty") }
    }else{
        println("SwitchChunkContainer can not be created. activeResult is null")
    }
}

private fun <DTO, D, E> sequencedSingle(
    serviceContext: ServiceContext<DTO, D, E>,
    block: SequenceChunkContainer<DTO, D>.(SingleTypeHandler<DTO, D>) -> DeferredContainer<ResultSingle<DTO, D>>
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel, E : LongEntity   {

    val execContext = serviceContext.dtoClass.executionContext
    val chunkContainer = SequenceChunkContainer(execContext, 100)
    block.invoke(chunkContainer, chunkContainer.singleTypeHandler)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}

private fun <DTO, D>  sequencedList(
    serviceContext: ServiceContext<DTO, D, *>,
    block: SequenceChunkContainer<DTO, D>.(ListTypeHandler<DTO, D>) -> DeferredContainer<ResultList<DTO, D>>
): SequenceChunkContainer<DTO, D> where DTO : ModelDTO, D : DataModel{

    val execContext = serviceContext.dtoClass.executionContext
    val chunkContainer = SequenceChunkContainer(execContext, 300)
    block.invoke(chunkContainer, chunkContainer.listTypeHandler)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: SingleDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SingleTypeHandler<DTO, D>) -> DeferredContainer<ResultSingle<DTO, D>>
) where DTO : ModelDTO, D : DataModel{
    val chunkContainer = sequencedSingle(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}


fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ListDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(ListTypeHandler<DTO, D>) -> DeferredContainer<ResultList<DTO, D>>
) where DTO : ModelDTO, D : DataModel
{
    val chunkContainer = sequencedList(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}
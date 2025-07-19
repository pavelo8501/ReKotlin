package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.helpers.asCommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations
import po.exposify.scope.sequence.launcher.ListDescriptor
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleDescriptor
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.exposify.scope.sequence.launcher.SwitchSingeDescriptor
import po.exposify.scope.sequence.models.SequenceParameter
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.LazyExecutionContainer


fun <DTO, D> ExecutionChunkBase<DTO, D>.withResult(
    block: ResultBase<DTO, D>.()-> Unit
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

fun <DTO, D, E, F, FD, FE> SingleResultChunks<F, FD>.switchStatement(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, E, F>,
    block: SwitchChunkContainer<DTO, D, E, F, FD, FE>.(SingleTypeSwitchHandler<DTO, D, E, F, FD, FE>) -> DeferredContainer<ResultSingle<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel, E: LongEntity, F: ModelDTO, FD: DataModel, FE: LongEntity{

    val consResult = activeResult
    if(consResult != null){
        consResult.getAsCommonDTO()?.let {foreignDTO->
            val castedForeign = foreignDTO.castOrOperations<CommonDTO<F, FD, FE>>(switchDescriptor)
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
    block: SequenceChunkContainer<DTO, D, E>.(SingleTypeHandler<DTO, D, E>) -> DeferredContainer<ResultSingle<DTO, D, *>>
): SequenceChunkContainer<DTO, D, E> where DTO : ModelDTO, D : DataModel, E : LongEntity   {

    val execContext = serviceContext.dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 100)
    block.invoke(chunkContainer, chunkContainer.singleTypeHandler)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}

private fun <DTO, D, E>  sequencedList(
    serviceContext: ServiceContext<DTO, D, E>,
    block: SequenceChunkContainer<DTO, D, E>.(ListTypeHandler<DTO, D, E>) -> DeferredContainer<ResultList<DTO, D, *>>
): SequenceChunkContainer<DTO, D, E> where DTO : ModelDTO, D : DataModel, E : LongEntity  {

    val execContext = serviceContext.dtoClass.createProvider()
    val chunkContainer = SequenceChunkContainer(execContext, 300)
    block.invoke(chunkContainer, chunkContainer.listTypeHandler)
    chunkContainer.chunks.forEach {chunk->
        chunk.configure()
    }
    return chunkContainer
}

fun <DTO, D, E> ServiceContext<DTO, D, E>.sequenced(
    launchDescriptor: SingleDescriptor<DTO, D, E>,
    block: SequenceChunkContainer<DTO, D, E>.(SingleTypeHandler<DTO, D, E>) -> DeferredContainer<ResultSingle<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel,  E : LongEntity{
    val chunkContainer = sequencedSingle(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}


fun <DTO, D, E> ServiceContext<DTO, D, E>.sequenced(
    launchDescriptor: ListDescriptor<DTO, D, E>,
    block: SequenceChunkContainer<DTO, D, E>.(ListTypeHandler<DTO, D, E>) -> DeferredContainer<ResultList<DTO, D, *>>
) where DTO : ModelDTO, D : DataModel, E: LongEntity
{
    val chunkContainer = sequencedList(this, block)
    launchDescriptor.registerChunkContainer(chunkContainer)
}
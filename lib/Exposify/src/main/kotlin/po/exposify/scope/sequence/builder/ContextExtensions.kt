package po.exposify.scope.sequence.builder


import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.IdLaunchDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSinge
import po.exposify.scope.sequence.models.SequenceParameter
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.PromiseResultContainer


//fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.returnResult(
//    block: ResultBase<DTO, D>.()-> ResultBase<DTO, D>
//)where DTO : ModelDTO, D : DataModel, P:Any {
//    println("Execution context on chunk result $executionContext")
//
//}

//fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withResult(
//    block: ResultBase<DTO, D>.()-> Unit
//) where DTO : ModelDTO, D : DataModel, P:Any {
//    getLastResult()?.block()
//}

fun <DTO, D, P> ExecutionChunkBase<DTO, D, P>.withInputValue(
    block:P.()-> Unit
) where DTO : ModelDTO, D : DataModel, P:Any {
    hooks.onResolve(block)
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    inputContainer: PromiseResultContainer<D>,
    actionBlock: ExecutionChunkBase<DTO, D, D>.()-> ResultBase<DTO, D>
) where DTO: ModelDTO, D: DataModel {

    val insert = InsertChunk.create(dtoClass, inputContainer,  executionContext, actionBlock)

    insert.onResultRequested{
        println("$insert evaluating result")
        executionContext.insert(it)
    }
    registerChunk(insert)
}


fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    inputData: PromiseResultContainer<Long>,
    inputContainer: PromiseResultContainer<Long>,
    block: ExecutionChunkBase<DTO, D, Long>.()->  ResultBase<DTO, D>
) where DTO: ModelDTO, D: DataModel {

    val pickById = PickByIdChunk.create(dtoClass,inputContainer,  executionContext, block)
    pickById.saveInputContainer(inputData)

    pickById.onResultRequested{
        println("$pickById evaluating result")
        executionContext.pickById(it)
    }
    registerChunk(pickById)
}

fun <DTO, D> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: IdLaunchDescriptor<DTO, D>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<Long>) -> Unit
) where DTO : ModelDTO, D : DataModel {

    val execContext = dtoClass.createProvider()
    val context = SequenceChunkContainer(dtoClass,execContext, 100)

    val container = PromiseResultContainer.create<Long>(this)
    val parameter = SequenceParameter(container)

    block.invoke(context, parameter)
    context.chunks.forEach {chunk->
        //launchDescriptor.registerChunk(it, container)
    }
}


fun <DTO, D, P> ServiceContext<DTO, D, *>.sequenced(
    launchDescriptor: ParametrizedSinge<DTO, D, P>,
    block: SequenceChunkContainer<DTO, D>.(SequenceParameter<P>) -> Unit
) where DTO : ModelDTO, D : DataModel, P: D {

    val execContext = dtoClass.createProvider()
    val context = SequenceChunkContainer(dtoClass,execContext, 100)
    val container = PromiseResultContainer.create<P>(this)
    val parameter = SequenceParameter(container)
    block.invoke(context, parameter)
    launchDescriptor.registerChunkContainer(context)
}
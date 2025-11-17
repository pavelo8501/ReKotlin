package po.exposify.scope.sequence.launcher

import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.SessionBase
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.ListDescriptor
import po.exposify.scope.sequence.builder.RootDescriptorBase
import po.exposify.scope.sequence.builder.SequenceChunkContainer
import po.exposify.scope.sequence.builder.SingleDescriptor
import po.exposify.scope.sequence.inputs.CommonInputType
import po.exposify.scope.sequence.inputs.DataInput
import po.exposify.scope.sequence.inputs.ListDataInput
import po.exposify.scope.sequence.inputs.ParameterInput
import po.exposify.scope.sequence.inputs.QueryInput
import po.exposify.scope.sequence.inputs.SingleInputType
import po.lognotify.launchers.runProcess
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.containers.DeferredContainer

private suspend fun <DTO, D, I> launchExecutionList(
    descriptor: RootDescriptorBase<DTO, D>,
    session: SessionBase,
    input: CommonInputType<I>?,
): ResultList<DTO, D> where DTO : ModelDTO, D : DataModel, I : Any {

    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"

    val errorFallback =
        ExceptionFallback {
            val noContainerMsg = "No predefined execution for $descriptor"
            OperationsException(descriptor, noContainerMsg, ExceptionCode.Sequence_Setup_Failure)
        }

    val container = descriptor.containerBacking.getValue(descriptor)

    val castedContainer = container.castOrOperations<SequenceChunkContainer<DTO, D>>(descriptor)

    val service = descriptor.dtoClass.serviceClass
    return runProcess(session){
        val emitter = service.requestEmitter(it)
         emitter.dispatchList {
                var effectiveResult: ResultList<DTO, D>? = null

                castedContainer.listResultChunks.forEach { chunk ->
                    if(input is ListDataInput<*,*>) {
                        effectiveResult =
                            chunk.triggerList(descriptor.dtoClass, input.getValue(descriptor.dtoClass.commonDTOType.dataType))
                    }
                    if(input is QueryInput<*,*>) {
                        effectiveResult = chunk.triggerList(descriptor.dtoClass, input.value)
                    }
                    if(input == null){
                        effectiveResult =  chunk.triggerList(descriptor.dtoClass)
                    }
                }

                effectiveResult.getOrOperations(descriptor)
            }
    }
}

suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    launchDescriptor: ListDescriptor<DTO, D>
): ResultList<DTO, D> = launchExecutionList<DTO, D, Unit>(launchDescriptor, this, null)

suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    launchDescriptor: ListDescriptor<DTO, D>,
    inputData: List<D>,
): ResultList<DTO, D> = launchExecutionList<DTO, D, List<D>>(launchDescriptor, this, ListDataInput(inputData, launchDescriptor))

suspend fun <DTO, D> SessionBase.launch(
    launchDescriptor: ListDescriptor<DTO, D>,
    deferredQuery: DeferredContainer<WhereQuery<*>>,
): ResultList<DTO, D> where DTO : ModelDTO, D : DataModel = launchExecutionList(launchDescriptor, this, QueryInput(deferredQuery, launchDescriptor))



internal suspend fun <DTO, D> launchExecutionSingle(
    session: SessionBase,
    descriptor: RootDescriptorBase<DTO, D>,
    input: CommonInputType<*>,
): ResultSingle<DTO, D> where DTO : ModelDTO, D : DataModel {
    val wrongBranchMsg = "LaunchExecutionResultSingle else branch should have never be reached"

    val errorFallback =
        ExceptionFallback {
            val noContainerMsg = "No predefined execution for $descriptor"
            OperationsException(descriptor, noContainerMsg, ExceptionCode.Sequence_Setup_Failure)
        }

    val container = descriptor.containerBacking.getValue(descriptor)
    val castedContainer = container.castOrOperations<SequenceChunkContainer<DTO, D>>(descriptor)

    val service = descriptor.dtoClass.serviceClass
    return  runProcess(session){
        val emitter = service.requestEmitter(it)
        emitter.dispatchSingle {
            var effectiveResult: ResultSingle<DTO, D>? = null
            castedContainer.singleResultChunks.forEach { chunk ->
                if(input is DataInput<*,*>) {
                    val value = input.getValue(descriptor.dtoClass.commonDTOType.dataType)
                    effectiveResult = chunk.triggerSingle(descriptor.dtoClass, value)
                }
                if(input is QueryInput<*,*>) {
                    effectiveResult = chunk.triggerSingle(descriptor.dtoClass, input.value)
                }
                if(input is ParameterInput<*, *>) {
                    effectiveResult =  chunk.triggerSingle(descriptor.dtoClass, input.value)
                }
            }
            effectiveResult.getOrOperations(descriptor)
        }
    }
}



suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    input: SingleInputType<DTO, D, *>
): ResultSingle<DTO, D> = launchExecutionSingle(this, input.descriptor.castOrInit<RootDescriptorBase<DTO, D>>(input.descriptor), input)


suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    launchDescriptor: SingleDescriptor<DTO, D>,
    parameter: Long,
): ResultSingle<DTO, D> = launchExecutionSingle(this, launchDescriptor, ParameterInput(parameter, launchDescriptor))

suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    launchDescriptor: SingleDescriptor<DTO, D>,
    inputData: D,
): ResultSingle<DTO, D> = launchExecutionSingle(this, launchDescriptor, DataInput(inputData, launchDescriptor))

suspend fun <DTO : ModelDTO, D : DataModel> SessionBase.launch(
    launchDescriptor: SingleDescriptor<DTO, D>,
    deferredQuery: DeferredContainer<WhereQuery<*>>,
): ResultSingle<DTO, D> = launchExecutionSingle(this, launchDescriptor, QueryInput(deferredQuery, launchDescriptor))



suspend fun <DTO : ModelDTO, D : DataModel, R> SessionBase.launch(
    input: SingleInputType<DTO, D, *>,
    block: suspend ResultSingle<DTO, D>.()-> R
):R {
   val result =  launchExecutionSingle(this, input.descriptor.castOrInit<RootDescriptorBase<DTO, D>>(input.descriptor), input)
   return result.block()
}
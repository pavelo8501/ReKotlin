package po.exposify.scope.sequence.launcher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResultList
import po.exposify.dto.components.result.toResultSingle
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.extensions.withSuspendedTransactionIfNone
import po.exposify.scope.sequence.builder.ListDescriptor
import po.exposify.scope.sequence.builder.PickChunk
import po.exposify.scope.sequence.builder.SelectChunk
import po.exposify.scope.sequence.builder.SingleDescriptor
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.SwitchDescriptorBase
import po.exposify.scope.sequence.builder.SwitchListDescriptor
import po.exposify.scope.sequence.builder.SwitchSingeDescriptor
import po.exposify.scope.sequence.builder.UpdateChunk
import po.exposify.scope.sequence.builder.UpdateListChunk
import po.exposify.scope.sequence.inputs.InputBase
import po.exposify.scope.sequence.inputs.DataInput
import po.exposify.scope.sequence.inputs.ListDataInput
import po.exposify.scope.sequence.inputs.ParameterInput
import po.exposify.scope.sequence.inputs.QueryInput
import po.exposify.scope.service.ServiceContext
import po.misc.functions.containers.DeferredContainer
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged


private fun <DTO:ModelDTO, D:DataModel> returnContainerNotFound(
    descriptor: SwitchListDescriptor<DTO, D, *, *>
): ResultList<DTO, D>{
    val message = "Unable to process $descriptor. No switchDTO statement found"
    val badSetup =  descriptor.initException(message, ExceptionCode.BAD_DTO_SETUP)
    return badSetup.toResultList(descriptor.dtoClass)
}

private fun <DTO:ModelDTO, D:DataModel> returnContainerNotFound(
    descriptor: SwitchDescriptorBase<DTO, D, *, *>
): ResultBase<DTO, D, *>{
    val message = "Unable to process $descriptor. No switchDTO statement found"
    val badSetup =  descriptor.initException(message, ExceptionCode.BAD_DTO_SETUP)
    return when(descriptor){
        is SwitchListDescriptor -> badSetup.toResultList(descriptor.dtoClass)
        is SwitchSingeDescriptor-> badSetup.toResultSingle(descriptor.dtoClass)
    }
}

private suspend fun <DTO, D, F, FD> launchSwitch(
    session:AuthorizedSession,
    descriptor: SwitchDescriptorBase<DTO, D,  F, FD>,
    inputData: InputBase<DTO, *>,
    parentInput: InputBase<F, *>
): ResultBase<DTO, D, *> where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {

    val container =  descriptor.containerBacking.value
    if(container == null){
        return  returnContainerNotFound(descriptor)
    }

    val castedContainer = container.castOrManaged<SwitchChunkContainer<DTO, D, F, FD>>(session)
    var effectiveResult: ResultBase<DTO, D, *>? = null

    withSuspendedTransactionIfNone(descriptor.dtoClass.debugger, false){
        if (parentInput.descriptor is SingleDescriptor) {

            val parentResult = launchExecutionSingle(session, parentInput.descriptor, parentInput)
            when (inputData) {
                is DataInput -> {
                    val chunksAcceptingDataInput =
                        castedContainer.singleResultChunks.filterIsInstance<UpdateChunk<DTO, D>>()
                    val input = inputData.getValue(descriptor.dtoClass.commonDTOType.dataType)
                    chunksAcceptingDataInput.forEach { chunk ->
                        effectiveResult = chunk.updateSwitching(parentResult, descriptor.dtoClass, input)
                    }
                }

                is ParameterInput -> {
                    val chunksAcceptingLong = castedContainer.singleResultChunks.filterIsInstance<PickChunk<DTO, D>>()
                    val input = inputData.value
                    chunksAcceptingLong.forEach { chunk ->
                        effectiveResult = chunk.pickSwitching(parentResult, descriptor.dtoClass, input)
                    }
                }

                is ListDataInput<*, *> -> {
                    val input = inputData.getValue(descriptor.dtoClass.commonDTOType.dataType)
                    val chunksAcceptingDataList =
                        castedContainer.listResultChunks.filterIsInstance<UpdateListChunk<DTO, D>>()
                    chunksAcceptingDataList.forEach { chunk ->
                        effectiveResult = chunk.updateSwitching(parentResult, descriptor.dtoClass, input)
                    }
                }
                is QueryInput<*, *> -> {
                    if(descriptor is SwitchSingeDescriptor){
                        val chunks = castedContainer.singleResultChunks.filterIsInstance<PickChunk<DTO, D>>()
                        chunks.forEach { chunk ->
                            effectiveResult = chunk.pickSwitching(parentResult, descriptor.dtoClass, inputData.value)
                        }
                    }else{
                        val chunks = castedContainer.listResultChunks.filterIsInstance<SelectChunk<DTO, D>>()
                        chunks.forEach { chunk ->
                            effectiveResult = chunk.selectSwitching(parentResult, descriptor.dtoClass, inputData.value)
                        }
                    }
                }
            }
        }else{
            TODO("Not yet supported")
        }
    }
    return effectiveResult.getOrManaged(ResultBase::class)
}



suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    id: Long,
    parentInput: InputBase<F, *>,
): ResultSingle<DTO, D> {
    val input = ParameterInput(id, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input, parentInput)
    result as ResultSingle<DTO, D>
    return result
}


suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    dataModel: D,
    parentInput: InputBase<F, *>,
): ResultSingle<DTO, D>{
    val input =  DataInput(dataModel, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input as InputBase<DTO, D>, parentInput)
    result as ResultSingle<DTO, D>
    return result
}

suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchListDescriptor<DTO, D, F, FD>,
    inputData: List<D>,
    parentInput: InputBase<F, *>,
): ResultList<DTO, D>
{
    val input = ListDataInput(inputData, switchDescriptor)
    val result = launchSwitch(this, switchDescriptor, input, parentInput)
    result as ResultList<DTO, D>
    return result
}

suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchListDescriptor<DTO, D, F, FD>,
    whereQuery: DeferredContainer<WhereQuery<*>>,
    parentInput: InputBase<F, *>,
): ResultList<DTO, D>
{
    val input = QueryInput(whereQuery, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input, parentInput)
    result as ResultList<DTO, D>
    return result
}


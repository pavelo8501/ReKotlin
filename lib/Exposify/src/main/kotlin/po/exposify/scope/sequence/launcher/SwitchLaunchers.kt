package po.exposify.scope.sequence.launcher

import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultBase
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResultList
import po.exposify.dto.components.result.toResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.operationsException
import po.exposify.scope.sequence.builder.RootDescriptorBase
import po.exposify.scope.sequence.builder.SwitchDescriptorBase
import po.exposify.scope.sequence.builder.SwitchListDescriptor
import po.exposify.scope.sequence.builder.SwitchSingeDescriptor
import po.exposify.scope.sequence.inputs.InputBase
import po.exposify.scope.sequence.inputs.DataInput
import po.exposify.scope.sequence.inputs.ListDataInput
import po.exposify.scope.sequence.inputs.ParameterInput
import po.exposify.scope.sequence.inputs.QueryInput
import po.exposify.scope.sequence.inputs.SwitchInputBase
import po.exposify.scope.sequence.inputs.SwitchListInput
import po.misc.functions.containers.DeferredContainer
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
    inputData: InputBase<DTO, D, *>,
    parentInput: InputBase<F, *, *>
): ResultBase<DTO, D, *> where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {


    val inputNotSupported = operationsException("parentInput must be of type RootDescriptorBase", ExceptionCode.BAD_DTO_SETUP, descriptor)

    val container = descriptor.switchContainerBacking.value ?: return returnContainerNotFound(descriptor)
    var effectiveResult: ResultBase<DTO, D, *>? = null


    if (parentInput.descriptor is  RootDescriptorBase) {
        TODO("Part of refactro")
        val parentResult = launchExecutionSingle(session, parentInput.descriptor as RootDescriptorBase<out Any?, out Any?>, parentInput)

//        when (inputData) {
//            is DataInput <*, *> -> {
//                val chunksAcceptingDataInput =
//                    container.singleResultChunks.filterIsInstance<UpdateChunk<DTO, D>>()
//                val input = inputData.getValue(descriptor.dtoClass.commonDTOType.dataType)
//                chunksAcceptingDataInput.forEach { chunk ->
//                    effectiveResult = chunk.updateSwitching(parentResult, descriptor.dtoClass, input)
//                }
//            }
//
//            is ParameterInput<*, *> -> {
//                val chunksAcceptingLong = container.singleResultChunks.filterIsInstance<PickChunk<DTO, D>>()
//                val input = inputData.value
//                chunksAcceptingLong.forEach { chunk ->
//                    effectiveResult = chunk.pickSwitching(parentResult, descriptor.dtoClass, input)
//                }
//            }
//
//            is ListDataInput<*, *> -> {
//                val input = inputData.getValue(descriptor.dtoClass.commonDTOType.dataType)
//                val chunksAcceptingDataList =
//                    container.listResultChunks.filterIsInstance<UpdateListChunk<DTO, D>>()
//                chunksAcceptingDataList.forEach { chunk ->
//                    effectiveResult = chunk.updateSwitching(parentResult, descriptor.dtoClass, input)
//                }
//            }
//
//            is QueryInput<*, *> -> {
//                if (descriptor is SwitchSingeDescriptor) {
//                    val chunks = container.singleResultChunks.filterIsInstance<PickChunk<DTO, D>>()
//                    chunks.forEach { chunk ->
//                        effectiveResult = chunk.pickSwitching(parentResult, descriptor.dtoClass, inputData.value)
//                    }
//                } else {
//                    val chunks = container.listResultChunks.filterIsInstance<SelectChunk<DTO, D>>()
//                    chunks.forEach { chunk ->
//                       // effectiveResult = chunk.selectSwitching(parentResult, descriptor.dtoClass, inputData.value)
//                    }
//                }
//            }
        }
//    } else {
//        effectiveResult = descriptor.dtoClass.toResult(inputNotSupported)
//    }
    return effectiveResult.getOrManaged(ResultBase::class)
}



suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    id: Long,
    parentInput: InputBase<F, *, *>,
): ResultSingle<DTO, D> {
    val input = ParameterInput(id, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input, parentInput)
    result as ResultSingle<DTO, D>
    return result
}


suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F, FD>,
    dataModel: D,
    parentInput: InputBase<F, *, *>,
): ResultSingle<DTO, D>{
    val input =  DataInput(dataModel, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input as InputBase<DTO, D, D>, parentInput)
    result as ResultSingle<DTO, D>
    return result
}

//suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
//    switchDescriptor: SwitchListDescriptor<DTO, D, F, FD>,
//    input: ListInputType<DTO, D, *>,
//    parentInput: InputBase<F, D, *>,
//): ResultList<DTO, D>
//{
//    val result = launchSwitch(this, switchDescriptor, input as InputBase<DTO, D, *>, parentInput)
//    result as ResultList<DTO, D>
//    return result
//}

suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> AuthorizedSession.launchSwitching(
    switchDescriptor: SwitchListDescriptor<DTO, D, F, FD>,
    inputData: List<D>,
    parentInput: InputBase<F, *, *>,
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
    parentInput: InputBase<F, *, *>,
): ResultList<DTO, D>
{
    val input = QueryInput(whereQuery, switchDescriptor)
    val result = launchSwitch<DTO, D, F, FD>(this, switchDescriptor, input, parentInput)
    result as ResultList<DTO, D>
    return result
}


private suspend fun <DTO, D, F, FD> launchSwitch2(
    input: SwitchInputBase<DTO, D, F, FD, *>,
    parentResult: CommonDTO<F, FD, *>
): ResultBase<DTO, D, *> where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {

    if (input.session is AuthorizedSession){
        println(input.session)
    }

    TODO("Part of refactro")

//    var effectiveResult: ResultBase<DTO, D, *>? = null
//
//    when(input.descriptor){
//        is SwitchListDescriptor<*, *, *, *> -> {
//            val listInput = input.castOrManaged<SwitchListInput<DTO, D, F, FD, *>>(input)
//            when(listInput){
//                is SelectAllInput<*, *, *, *> -> {
//                   val container = listInput.descriptor.switchContainerBacking.getValue(input)
//                    val chunks = container.listResultChunks.filterIsInstance<SelectChunk<DTO, D>>()
//                    chunks.forEach { chunk->
//                        effectiveResult =  chunk.selectSwitching(parentResult,  listInput.descriptor.dtoClass)
//                    }
//                }
//            }
//        }
//        is SwitchSingeDescriptor<*, *, *, *> ->{
//
//        }
//    }
//
//    return effectiveResult.getOrManaged(ResultBase::class)
}


suspend fun <DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel> ResultSingle<F, FD>.launchSwitching(
    input: SwitchListInput<DTO, D, F, FD, *>
): ResultList<DTO, D>{
    val result = getAsCommonDTO()
    return launchSwitch2(input as SwitchInputBase<DTO, D, F, FD, *>, result) as ResultList<DTO, D>
}



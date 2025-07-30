package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.SingleResultChunks
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.UpdateChunk
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.containers.DeferredContainer


fun <DTO, D, E, F, FD> handleDataInputs(
    container: SwitchChunkContainer<DTO, D, F, FD>,
    parameter: Long? = null,
    inputData:D? = null,
    query: DeferredContainer<WhereQuery<E>>? = null,
) where DTO : ModelDTO, D : DataModel, E: LongEntity,  F : ModelDTO, FD : DataModel {


}


//suspend fun <DTO, D, F, FD> launchSwitch(
//    switchDescriptor: SwitchDescriptorBase<DTO, D, F>,
//    input: D,
//    parentDescriptor: SingleDescriptor<F, FD>,
//    parentParameter: Long,
//    session: AuthorizedSession
//): ResultSingle<DTO, D> where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {
//
//    val wrongBranchMsg = "LaunchSwitch else branch should have never be reached"
//    val result = session.launch(parentDescriptor, parentParameter)
//
//
//    val errorFallback = ExceptionFallback{
//        val noContainerMsg = "No predefined execution for $switchDescriptor"
//        OperationsException(noContainerMsg, ExceptionCode.Sequence_Setup_Failure, switchDescriptor)
//    }
//
//    val container =  switchDescriptor.chunksContainerBacking.getWithFallback(errorFallback)
//    val service =    switchDescriptor.dtoClass.serviceClass
//
//    var activeResult: ResultSingle<DTO, D>? = null
//
//    val switchContainers = container.singleSwitchContainers<DTO, D>(switchDescriptor.inputType)
//    if(switchContainers.isNotEmpty()) {
//        switchContainers.forEach { switchContainer ->
//
//            handleDataInputs(switchContainer, inputData = input)
//            switchContainer.chunks.forEach { chunk ->
//                when (chunk) {
//                    is SingleResultChunks<*, *>->{
//                        val updateChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(switchDescriptor)
//                        updateChunk.healthMonitor.print()
//                        activeResult = updateChunk.computeResult()
//                    }
//                    else -> {
//                        throw switchDescriptor.operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
//                    }
//                }
//            }
//        }
//    }else{
//        println("No switch containers for switchContainer ")
//        println("Total containers count ${container.chunkCollectionSize}")
//    }
//    return activeResult.getOrOperations(switchDescriptor)
//}


suspend fun <DTO, D, F, FD> launchSwitchSingle(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F>,
    parentDescriptor:SequenceDescriptor<F, FD>,
    session: AuthorizedSession,
    inputData: D? = null,
    parameter: Long? = null,
): ResultSingle<DTO, D> where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel {

    TODO("Not yet")
}


suspend fun <DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel> AuthorizedSession.launch(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F>,
    parentDescriptor:SequenceDescriptor<F, FD>,
    parameter: Long
): ResultSingle<DTO, D> = launchSwitchSingle(switchDescriptor, parentDescriptor, this,  parameter = parameter)


suspend fun <DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel>  AuthorizedSession.launch(
    switchDescriptor: SwitchSingeDescriptor<DTO, D, F>,
    parentDescriptor:SequenceDescriptor<F, FD>,
    inputData: D,
): ResultSingle<DTO, D> = launchSwitchSingle(switchDescriptor, parentDescriptor,this, inputData = inputData)




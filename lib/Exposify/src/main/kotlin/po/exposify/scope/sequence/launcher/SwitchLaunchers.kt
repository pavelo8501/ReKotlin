package po.exposify.scope.sequence.launcher

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.PickByIdChunk
import po.exposify.scope.sequence.builder.SelectChunk
import po.exposify.scope.sequence.builder.SingleResultChunks
import po.exposify.scope.sequence.builder.SwitchChunkContainer
import po.exposify.scope.sequence.builder.UpdateChunk
import po.exposify.scope.sequence.builder.UpdateListChunk
import po.misc.functions.containers.DeferredContainer


fun <DTO, D, E, F, FD> handleDataInputs(
    container: SwitchChunkContainer<DTO, D, E, F, FD, *>,
    parameter: Long? = null,
    inputData:D? = null,
    query: DeferredContainer<WhereQuery<E>>? = null,
) where DTO : ModelDTO, D : DataModel, E: LongEntity,  F : ModelDTO, FD : DataModel {



}


suspend fun <DTO, D, E, F, FD> launchSwitch(
    switchDescriptor: SwitchDescriptorBase<DTO, D, E, F>,
    input: D,
    parentDescriptor: SingleDescriptor<F, FD, *>,
    parentParameter: Long,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO : ModelDTO, D : DataModel, E: LongEntity,  F : ModelDTO, FD : DataModel {


    val wrongBranchMsg = "LaunchSwitch else branch should have never be reached"
    val result = launch(parentDescriptor, parentParameter, session)
    val container = parentDescriptor.container
    var activeResult: ResultSingle<DTO, D, *>? = null
    val switchContainers = container.singleSwitchContainers<DTO, D, E>(switchDescriptor.inputType)
    if(switchContainers.isNotEmpty()) {
        switchContainers.forEach { switchContainer ->


            handleDataInputs(switchContainer, inputData = input)
            switchContainer.chunks.forEach { chunk ->
                when (chunk) {
                    is SingleResultChunks<*, *>->{
                        val updateChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(switchDescriptor)
                        updateChunk.healthMonitor.print()
                        activeResult = updateChunk.computeResult()
                    }
                    else -> {
                        throw switchDescriptor.operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
                    }
                }
            }
        }
    }else{
        println("No switch containers for switchContainer ${switchDescriptor.parameterType}")
        println("Total containers count ${container.chunkCollectionSize}")
    }
    return activeResult.getOrOperations(switchDescriptor)
}




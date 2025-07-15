package po.exposify.scope.sequence.launcher

import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.builder.UpdateChunk

fun <DTO, D, F, FD> launchSwitch(
    switchDescriptor: ParametrizedSwitchSinge<DTO, D>,
    parameter: D,
    parentDescriptor: LongSingeDescriptor<F, FD>,
    parentParameter: Long,
    session: AuthorizedSession
): ResultSingle<DTO, D, *> where DTO : ModelDTO, D : DataModel,  F : ModelDTO, FD : DataModel {
    val wrongBranchMsg = "LaunchSwitch else branch should have never be reached"
    val result = launch(parentDescriptor, parentParameter, session)
    val container = parentDescriptor.container

    var activeResult: ResultSingle<DTO, D, *>? = null

    val switchContainer = container.findFirstSwitchContainer<DTO, D>(switchDescriptor.parameterType, true)
    if (switchContainer != null) {
        switchContainer.chunks.forEach { chunk ->
            when (chunk) {
                is UpdateChunk<*, *> -> {
                    val updateChunk = chunk.castOrOperations<UpdateChunk<DTO, D>>(switchDescriptor)
                    updateChunk.healthMonitor.print()
                    updateChunk.inputContainer.registerProvider { parameter }
                    activeResult = updateChunk.computeResult()
                }
                else -> throw operationsException(wrongBranchMsg, ExceptionCode.ABNORMAL_STATE)
            }
        }
    } else {
        println("No switch containers for switchContainer ${switchDescriptor.parameterType}")
        println("Total containers count ${container.chunkCollectionSize}")
    }
    return activeResult.getOrOperations("activeResult")
}




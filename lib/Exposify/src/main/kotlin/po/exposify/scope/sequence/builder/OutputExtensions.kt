package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.misc.functions.containers.DeferredContainer



fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D>> where DTO: ModelDTO, D: DataModel
{
    val pickByIdChunk = PickByIdChunk.create(configurationBlock)

    pickByIdChunk.resultContainer.registerProvider {
        val parameter = handler.deferredParameter.resolve()
        execContext.pickById(parameter)
    }
    registerChunk(pickByIdChunk)
    return pickByIdChunk.resultContainer
}


fun <DTO, D> SequenceChunkContainer<DTO, D>.select(
    handler: ListTypeHandler<DTO, D>,
    configurationBlock: ListResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultList<DTO, D>> where DTO: ModelDTO, D: DataModel
{
    val selectChunk = SelectChunk.create(configurationBlock)
    selectChunk.resultContainer.registerProvider {
        val result = if (handler.isWhereQueryAvailable) {
            execContext.select(handler.whereQuery.resolve())
        } else {
            execContext.select()
        }
        result
    }
    registerChunk(selectChunk)
    return selectChunk.resultContainer
}

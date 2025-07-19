package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.misc.functions.containers.DeferredContainer



fun <DTO, D, E> SequenceChunkContainer<DTO, D, E>.pickById(
    handler: SingleTypeHandler<DTO, D, E>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D, *>> where DTO: ModelDTO, D: DataModel, E : LongEntity
{
    val pickByIdChunk = PickByIdChunk.create(configurationBlock)

    pickByIdChunk.resultContainer.registerProvider {
        val parameter = handler.deferredParameter.resolve()
        execContext.pickById(parameter)
    }
    registerChunk(pickByIdChunk)
    return pickByIdChunk.resultContainer
}


fun <DTO, D, E> SequenceChunkContainer<DTO, D, E>.select(
    handler: ListTypeHandler<DTO, D, E>,
    configurationBlock: ListResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultList<DTO, D, *>> where DTO: ModelDTO, D: DataModel, E : LongEntity
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

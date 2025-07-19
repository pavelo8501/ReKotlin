package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResultSingle
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.misc.functions.containers.DeferredContainer



fun <DTO, D, E> SequenceChunkContainer<DTO, D, E>.update(
    handler: SingleTypeHandler<DTO, D, E>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D, *>>
        where DTO: ModelDTO, D: DataModel, E : LongEntity{

    val updateChunk = UpdateChunk.create(configurationBlock)
    updateChunk.resultContainer.registerProvider{
        val inputData =  handler.deferredInput.resolve()
        execContext.updateSingle(inputData, this)
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}

fun <DTO, D, E> SequenceChunkContainer<DTO, D, E>.update(
    handler: ListTypeHandler<DTO, D, E>,
    configurationBlock: ListResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultList<DTO, D, *>>
        where DTO: ModelDTO, D: DataModel, E : LongEntity
{
    val updateListChunk = UpdateListChunk.create(configurationBlock)
    updateListChunk.resultContainer.registerProvider {
        val inputData =  handler.deferredInput.resolve()
        execContext.update(inputData, this)
    }

    registerChunk(updateListChunk)
    return updateListChunk.resultContainer
}


fun <DTO, D, E> SequenceChunkContainer<DTO, D, E>.insert(
    handler: SingleTypeHandler<DTO, D, E>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D, *>> where DTO : ModelDTO, D : DataModel, E : LongEntity = update(handler, configurationBlock)



fun <DTO, D, E, F, FD, FE> SwitchChunkContainer<DTO, D, E, F, FD, FE>.update(
    handler: SingleTypeSwitchHandler<DTO, D, E, F, FD, FE>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D, *>>
        where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO, FD: DataModel, FE: LongEntity {

    val updateChunk = UpdateChunk.create(configurationBlock)

    updateChunk.resultContainer.registerProvider {
        val inputData = handler.deferredInput.resolve()
        val executionContext = handler.parentDTO.getExecutionContext(handler.descriptor.dtoClass)
        executionContext?.updateSingle(inputData, this) ?: run {
            val exception = operationsException("Execution context not found", ExceptionCode.INVALID_DATA)
            exception.toResultSingle(CrudOperation.Update, handler.descriptor.dtoClass)
        }
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}
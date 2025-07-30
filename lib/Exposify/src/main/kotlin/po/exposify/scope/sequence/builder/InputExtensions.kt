package po.exposify.scope.sequence.builder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.launcher.ListTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeHandler
import po.exposify.scope.sequence.launcher.SingleTypeSwitchHandler
import po.misc.functions.containers.DeferredContainer


fun <DTO, D> SequenceChunkContainer<DTO, D>.update(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D>>
        where DTO: ModelDTO, D: DataModel{

    val updateChunk = UpdateChunk.create(configurationBlock)
    updateChunk.resultContainer.registerProvider{
        val inputData =  handler.deferredInput.resolve()
        val result = execContext.updateSingle(inputData, this)
        result
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.update(
    handler: ListTypeHandler<DTO, D>,
    configurationBlock: ListResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultList<DTO, D>> where DTO: ModelDTO, D: DataModel
{
    val updateListChunk = UpdateListChunk.create(configurationBlock)
    updateListChunk.resultContainer.registerProvider {
        val inputData =  handler.deferredInput.resolve()
        execContext.update(inputData, this)
    }

    registerChunk(updateListChunk)
    return updateListChunk.resultContainer
}


fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    handler: SingleTypeHandler<DTO, D>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D>> where DTO : ModelDTO, D : DataModel = update(handler, configurationBlock)



fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.update(
    handler: SingleTypeSwitchHandler<DTO, D, F, FD>,
    configurationBlock: SingleResultChunks<DTO, D>.()->  Unit
): DeferredContainer<ResultSingle<DTO, D>>
        where DTO: ModelDTO, D: DataModel, F : ModelDTO, FD: DataModel{

    val updateChunk = UpdateChunk.create(configurationBlock)

    //updateChunk.resultContainer.registerProvider {
      //  val inputData = handler.deferredInput.resolve()
       // val executionContext = handler.parentDTO.getExecutionContext(handler.descriptor.dtoClass.commonDTOType)
//        parentDTO.withDTOContext(handler.descriptor.dtoClass.commonDTOType){
//            updateSingle(inputData, this)
//
//
//        }
//
//        executionContext?.updateSingle(inputData, this) ?: run {
//            val exception = operationsException("Execution context not found", ExceptionCode.INVALID_DATA)
//            exception.toResultSingle(CrudOperation.Update, handler.descriptor.dtoClass)
//        }
//    }

   // registerChunk(updateChunk)
   // return updateChunk.resultContainer
    TODO("Not yet")
}
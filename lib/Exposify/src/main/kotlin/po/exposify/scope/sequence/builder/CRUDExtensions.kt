package po.exposify.scope.sequence.builder

import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.functions.containers.DeferredContainer
import po.misc.functions.containers.LambdaContainer
import po.misc.functions.containers.LazyExecutionContainer

fun <DTO, D> SequenceChunkContainer<DTO, D>.insert(
    inputContainer: LambdaContainer<D>,
    configurationBlock: SingleResultChunks<DTO, D, D>.()->  Unit
): LazyExecutionContainer<D, ResultSingle<DTO, D, *>> where DTO : ModelDTO, D : DataModel{

    val insertChunk = UpdateChunk.create(configurationBlock)
    insertChunk.resultContainer.registerProvider{data->
        val result = execContext.insert(data)
        result
    }
    registerChunk(insertChunk)
    return insertChunk.resultContainer
}

fun <DTO, D, F, FD> SwitchChunkContainer<DTO, D, F, FD>.update(
    inputContainer: LambdaContainer<FD>,
    configurationBlock: SingleResultChunks<F, FD, FD>.()->  Unit
): LazyExecutionContainer<FD, ResultSingle<F, FD, *>>
        where DTO: ModelDTO, D: DataModel, F : ModelDTO, FD: DataModel{

    val updateChunk = UpdateChunk.create(configurationBlock)
    updateChunk.resultContainer.registerProvider{ data ->
        val result = execContext.updateSingle(data, this)
        result
    }
    registerChunk(updateChunk)
    return updateChunk.resultContainer
}


fun <DTO, D> SequenceChunkContainer<DTO, D>.pickById(
    paramContainer: LambdaContainer<Long>,
    configurationBlock: SingleResultChunks<DTO, D, Long>.()->  Unit
): LazyExecutionContainer<Long, ResultSingle<DTO, D, *>> where DTO: ModelDTO, D: DataModel
{
    val pickByIdChunk = PickByIdChunk.create(configurationBlock)
    pickByIdChunk.resultContainer.registerProvider{id->
        val result = execContext.pickById(id)
        result
    }
    registerChunk(pickByIdChunk)

    return pickByIdChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.select(
    configurationBlock: ListResultChunks<DTO, D, Unit>.()->  Unit
): LazyExecutionContainer<Unit, ResultList<DTO, D, *>> where DTO: ModelDTO, D: DataModel
{
    val selectChunk = SelectChunk.create(configurationBlock)
    selectChunk.resultContainer.registerProvider {
        val result = execContext.select()
        result
    }
    registerChunk(selectChunk)
    return selectChunk.resultContainer
}

fun <DTO, D> SequenceChunkContainer<DTO, D>.select(
    deferredQuery: DeferredContainer<WhereQuery<*>>,
    configurationBlock: ListResultChunks<DTO, D, Unit>.()->  Unit
): LazyExecutionContainer<Unit, ResultList<DTO, D, *>> where DTO: ModelDTO, D: DataModel
{
    val selectChunk = SelectChunk.create(configurationBlock)
    selectChunk.resultContainer.registerProvider {
        val result = execContext.select()
        result
    }
    registerChunk(selectChunk)
    return selectChunk.resultContainer
}

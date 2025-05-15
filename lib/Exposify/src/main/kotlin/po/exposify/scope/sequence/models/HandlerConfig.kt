package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.SequenceHandlerBase


class HandlerConfig<DTO, D, E>(private val owningHandler : SequenceHandlerBase<DTO, D, E>)
        where DTO: ModelDTO, D: DataModel, E: LongEntity{

    fun withData(inputList : List<D>){
        owningHandler.inputListSource.clear()
        owningHandler.inputListSource.addAll(inputList)
    }

    @JvmName("withDataSingle")
    fun withData(data : D){
        owningHandler.inputDataSource = data
        owningHandler.inputListSource.clear()
        owningHandler.inputListSource.add(data)
    }

    fun withQuery(query : SimpleQuery){
        owningHandler.whereQueryParameter =  query
    }

    fun onResultCollected(resultCallback: (ResultList<DTO, D, E>)-> Unit){
        owningHandler.collectListResultFn = resultCallback
    }

    @JvmName("onResultCollectedSingle")
    fun onResultCollected(resultCallback: (ResultSingle<DTO, D, E>)-> Unit){
        owningHandler.collectSingleResultFn = resultCallback
    }

}
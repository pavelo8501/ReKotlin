package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SequenceHandlerBase


sealed class HandlerConfigBase<DTO, D, E>() where DTO: ModelDTO, D: DataModel, E: LongEntity{

    private var inputListParameter : List<D>? = null
    internal val inputList: List<D>
        get() = inputListParameter
            ?: inputDataSource?.let { listOf(it) }
            ?: throw InitException("InputList used but not provided", ExceptionCode.VALUE_IS_NULL)


    private var inputDataSource: D? = null
    internal val inputData : D
        get() = inputDataSource
            ?: inputListParameter?.firstOrNull()
            ?: throw InitException("InputData used but not provided", ExceptionCode.VALUE_IS_NULL)

    fun withData(list : List<D>){
        inputListParameter = list
    }
    @JvmName("withDataSingle")
    fun withData(data : D){
        inputDataSource = data
        inputListParameter = listOf(data)
    }

    var collectListResultFn : ((ResultList<DTO, D, E>)-> Unit)? = null
    fun onResultCollected(resultCallback: (ResultList<DTO, D, E>)-> Unit){
        collectListResultFn = resultCallback
    }

    var collectSingleResultFn: ((ResultSingle<DTO, D, E>)-> Unit)? = null
    @JvmName("onResultCollectedSingle")
    fun onResultCollected(resultCallback: (ResultSingle<DTO, D, E>)-> Unit){
        collectSingleResultFn = resultCallback
    }

    private val subHandlers : MutableMap<String, SequenceHandlerBase<*,*,*>> = mutableMapOf()
    internal fun registerSubHandler(name: String, subHandler: SequenceHandlerBase<*,*,*>){
        subHandlers[name] = subHandler
    }

}

class RootHandlerConfig<DTO, D, E>() : HandlerConfigBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

}

class ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>() : HandlerConfigBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity,
            F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{

    private var rootHandlerParameter : RootSequenceHandler<F_DTO, FD, FE>? = null
    val rootHandler  : RootSequenceHandler<F_DTO, FD, FE> get() = rootHandlerParameter.getOrOperationsEx()
    internal fun registerRootHandler(handler: RootSequenceHandler<F_DTO, FD, FE>){
        rootHandlerParameter = handler
    }

}
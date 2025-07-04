package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrInit
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import kotlin.collections.set


sealed class HandlerConfigBase<DTO, D, E>() where DTO: ModelDTO, D: DataModel, E: LongEntity{

    private var inputListParameter : List<D>? = null
    internal val inputList: List<D>
        get() = inputListParameter
            ?: inputDataSource?.let { listOf(it) }
            ?: throw InitException("InputList used but not provided", ExceptionCode.VALUE_IS_NULL, null)


    private var inputDataSource: D? = null
    internal val inputData : D
        get() = inputDataSource
            ?: inputListParameter?.firstOrNull()
            ?: throw InitException("InputData used but not provided", ExceptionCode.VALUE_IS_NULL, null)

    private var queryParameterProvider: (() -> SimpleQuery)? = null
    internal val query : SimpleQuery
        get() {
          val provider =  queryParameterProvider.getOrInit("Query used but not provided", null)
          return provider()
        }

    fun withData(list : List<D>){
        inputListParameter = list
    }
    @JvmName("withDataSingle")
    fun withData(data : D){
        inputDataSource = data
        inputListParameter = listOf(data)
    }

    fun withQuery(queryProvider : () -> SimpleQuery){
        queryParameterProvider = queryProvider
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

    private val switchHandlers : MutableMap<String, ClassSequenceHandler<*,*,*, DTO, D, E>> = mutableMapOf()
    internal fun registerSwitchHandler(name: String, subHandler: ClassSequenceHandler<*,*,*,  DTO, D, E>){
        switchHandlers[name] = subHandler
    }

    internal fun getSwitchHandler(name: String): ClassSequenceHandler<*,*,*, DTO, D, E>?{
        return switchHandlers[name]
    }

    internal var onStartCallback :  ((contextInfo : RunnableContext)->Unit)? = null
    fun onStart(callback :  (contextInfo : RunnableContext)->Unit){
        onStartCallback = callback
    }

    internal var onCompleteCallback : ((contextInfo : RunnableContext)->Unit)? = null
    fun onComplete(callback :  (contextInfo : RunnableContext)->Unit){
        onCompleteCallback = callback
    }
}

class RootHandlerConfig<DTO, D, E>() : HandlerConfigBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{

}

class ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>() : HandlerConfigBase<DTO, D, E>()
        where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    internal var rootHandlerParameter : RootSequenceHandler<F_DTO, FD, FE>? = null
    val rootHandler  : RootSequenceHandler<F_DTO, FD, FE>
        get() = rootHandlerParameter.getOrOperations("Root handler not found")

    internal fun registerRootHandler(handler: RootSequenceHandler<F_DTO, FD, FE>){
        rootHandlerParameter = handler
    }

}
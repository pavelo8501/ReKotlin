package po.exposify.scope.sequence.classes

import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.SwitchData
import po.misc.exceptions.CoroutineInfo
import kotlin.Long
import kotlin.coroutines.coroutineContext



fun <DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity> RootDTO<DTO, DATA, ENTITY>.createHandler(
    sequenceID: SequenceID
): RootSequenceHandler<DTO, DATA, ENTITY> {
    return RootSequenceHandler(sequenceID, this)
}


fun <DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity> DTOClass<DTO, DATA, ENTITY>.createHandler(
    sequenceID: SequenceID
): ClassSequenceHandler<DTO, DATA, ENTITY>{
    return  ClassSequenceHandler(sequenceID, this)
}



class SequenceHandler<DTO, DATA, ENTITY>(
    val dtoClass: DTOClass<DTO, DATA, ENTITY>,

) where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity {



}


class RootSequenceHandler<DTO, DATA, ENTITY>(
    sequenceId : SequenceID,
    val dtoClass: RootDTO<DTO, DATA, ENTITY>
):SequenceHandlerBase<DTO, DATA, ENTITY>(sequenceId, dtoClass) where DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity{

    override val switchParameters: MutableList<SwitchData<*, *, *>> = mutableListOf()

    internal var onStartCallback :  (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onStart(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onStartCallback = callback
    }

    internal var onCompleteCallback : (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onComplete(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onCompleteCallback = callback
    }
}

class ClassSequenceHandler<DTO, DATA, ENTITY>(
    sequenceId : SequenceID,
    val dtoClass: DTOClass<DTO, DATA, ENTITY>,
):SequenceHandlerBase<DTO, DATA, ENTITY>(sequenceId, dtoClass) where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity {
    override val switchParameters: MutableList<SwitchData<*, *, *>> = mutableListOf()
}

sealed class SequenceHandlerBase<DTO, DATA, ENTITY>(
    val sequenceId : SequenceID,
    val dtoBaseClass: DTOBase<DTO, DATA, ENTITY>
) where  DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity{


    internal abstract val switchParameters: MutableList<SwitchData<*, *, *>>


    protected val inputListSource : MutableList<DATA> = mutableListOf()
    val inputList : List<DATA>  get () = inputListSource.toList()

    protected var inputDataSource : DATA ?  =null
    val inputData: DATA  get() = inputDataSource?:throw OperationsException(
        "Input data used but not provided",
        ExceptionCode.VALUE_NOT_FOUND)


    protected var whereQueryParameter: SimpleQuery? = null
    val inputQuery: SimpleQuery get() = whereQueryParameter.getOrOperationsEx("Query parameter requested but uninitialized")

    protected var sequenceContext : SequenceContext<DTO, DATA, ENTITY>? = null

    private var collectListResultFn: ((ResultList<DTO, DATA, ENTITY>)-> Unit)? = null
    fun onResultCollected(resultCallback: (ResultList<DTO, DATA, ENTITY>)-> Unit){
        collectListResultFn = resultCallback
    }
    private var collectSingleResultFn: ((ResultSingle<DTO, DATA, ENTITY>)-> Unit)? = null
    @JvmName("onResultCollectedSingle")
    fun onResultCollected(resultCallback: (ResultSingle<DTO, DATA, ENTITY>)-> Unit){
        collectSingleResultFn = resultCallback
    }

    fun collectResult(result : ResultList<DTO, DATA, ENTITY>){
        collectListResultFn?.invoke(result)
    }
    fun collectResult(result : ResultSingle<DTO, DATA, ENTITY>){
        collectSingleResultFn?.invoke(result)
    }


//    private var switchQueryParameter : SwitchQuery<DATA, ENTITY>? = null
//    val switchQuery : SwitchQuery<DATA, ENTITY>
//        get() = switchQueryParameter.getOrOperationsEx("SwitchQuery not set")



    private var dataResult : List<DATA> = emptyList()
    internal fun provideContext(context : SequenceContext<DTO, DATA, ENTITY>){
        sequenceContext = context
        context.onResultUpdated = {
            dataResult = it.getData()
        }
    }
    fun getDataResult(): List<DATA>{
        return dataResult
    }

    fun withData(inputData : List<DATA>){
        inputListSource.clear()
        inputListSource.addAll(inputData)
    }

    fun withData(data : DATA){
        inputDataSource = data
        inputListSource.clear()
        inputListSource.add(data)
    }

    fun <T: IdTable<Long>> withQuery(where: WhereQuery<T>){
        whereQueryParameter =  where
    }

//    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>  SequenceHandlerBase<DTO, DATA, ENTITY>.switchQuery(
//        childClass: DTOClass<F_DTO, FD, FE>,
//        query: SwitchQuery<DTO,DATA, ENTITY>,
//        handlerBlock : suspend ClassSequenceHandler<F_DTO, FD, FE>.()-> Unit,
//    ){
//        val newChildHandler = childClass.createHandler(this.sequenceId)
//        val switchData =  SwitchData(newChildHandler, query, handlerBlock)
//        switchParameters.add(switchData)
//    }


    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>  SequenceHandlerBase<DTO, DATA, ENTITY>.switchParameters(
        childClass: DTOClass<F_DTO, FD, FE>,
        handlerBlock : suspend ClassSequenceHandler<F_DTO, FD, FE>.()-> Unit,
    ){
        val switchData =  SwitchData(childClass, handlerBlock)
        switchParameters.add(switchData)
    }

}


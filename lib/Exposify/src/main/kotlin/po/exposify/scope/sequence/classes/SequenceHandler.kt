package po.exposify.scope.sequence.classes

import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.Query
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
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

    protected val inputDataSource : MutableList<DATA> = mutableListOf()
    val inputData : List<DATA>  get () = inputDataSource.toList()

    protected var whereQueryParameter: Query? = null
    val inputQuery: Query get() = whereQueryParameter.getOrOperationsEx("Query parameter requested but uninitialized")

//    private var switchQueryParameter : SwitchQuery<DATA, ENTITY>? = null
//    val switchQuery : SwitchQuery<DATA, ENTITY>
//        get() = switchQueryParameter.getOrOperationsEx("SwitchQuery not set")

    protected var sequenceContext : SequenceContext<DTO, DATA, ENTITY>? = null

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
        inputDataSource.clear()
        inputDataSource.addAll(inputData)
    }

    fun withData(inputData : DATA){
        inputDataSource.clear()
        inputDataSource.add(inputData)
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

    private val deferredResult = CompletableDeferred<List<DATA>>()
    fun submitData(result: ResultList<DTO, DATA, ENTITY>){
        deferredResult.complete(result.getData())
    }
}


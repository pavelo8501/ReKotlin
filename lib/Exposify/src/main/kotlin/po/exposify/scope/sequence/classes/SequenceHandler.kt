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
import po.misc.exceptions.CoroutineInfo
import kotlin.Long
import kotlin.coroutines.coroutineContext


fun <DTO : ModelDTO, DATA: DataModel> DTOClass<DTO, DATA>.createHandler(
    sequenceID: SequenceID
)
: ClassSequenceHandler<DTO, DATA>{
    return  ClassSequenceHandler(this)
}

fun <DTO : ModelDTO, DATA: DataModel> RootDTO<DTO, DATA>.createHandler(
    sequenceID: SequenceID
): RootSequenceHandler<DTO, DATA> {
    return RootSequenceHandler(this)
}

class ClassSequenceHandler<DTO, DATA>(
    val dtoClass: DTOClass<DTO, DATA>,
):SequenceHandlerBase<DTO, DATA>(dtoClass) where DTO: ModelDTO, DATA: DataModel {

}

class RootSequenceHandler<DTO, DATA>(
    val dtoClass: RootDTO<DTO, DATA>
):SequenceHandlerBase<DTO, DATA>(dtoClass) where DTO: ModelDTO, DATA: DataModel{





    internal var onStartCallback :  (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onStart(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onStartCallback = callback
    }

    internal var onCompleteCallback : (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onComplete(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onCompleteCallback = callback
    }
}

sealed class SequenceHandlerBase<DTO, DATA>(
    val dtoBaseClass: DTOBase<DTO, DATA>
) where  DTO : ModelDTO, DATA : DataModel{

    protected val inputDataSource : MutableList<DATA> = mutableListOf()
    val inputData : List<DATA>  get () = inputDataSource.toList()

    protected var whereQueryParameter: Query? = null
    val inputQuery: Query get() = whereQueryParameter.getOrOperationsEx("Query parameter requested but uninitialized")

    protected var sequenceContext : SequenceContext<DTO, DATA, LongEntity>? = null

    private var dataResult : List<DATA> = emptyList()
    internal fun provideContext(context : SequenceContext<DTO, DATA, LongEntity>){
        sequenceContext = context
        context.onResultUpdated = {
            dataResult = it.getData()
        }
    }
    fun getDataResult(): List<DATA>{
        return dataResult
    }

    suspend fun launchWithData(inputData : List<DATA>){
        inputDataSource.clear()
        inputDataSource.addAll(inputData)
        println(CoroutineInfo.createInfo(coroutineContext))
    }

    fun <T: IdTable<Long>> launchWithQuery(where: WhereQuery<T>){
        whereQueryParameter =  where
    }

    private val deferredResult = CompletableDeferred<List<DATA>>()
    fun submitData(result: ResultList<*, DATA>){
        deferredResult.complete(result.getData())
    }

}


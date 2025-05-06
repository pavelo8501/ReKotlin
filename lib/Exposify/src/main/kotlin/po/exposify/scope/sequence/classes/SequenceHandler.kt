package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.DTOBase
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.service.ServiceContext
import po.misc.collections.generateKey
import kotlin.Long


fun <DTO : ModelDTO, DATA: DataModel> DTOBase<DTO, DATA>.createHandler(
    sequenceID: SequenceID
): SequenceHandler<DTO, DATA>{
    return  SequenceHandler(this)
}


class SequenceHandler<DTO, DATA>(
    val dtoClass: DTOBase<DTO, DATA>,
)where DTO: ModelDTO, DATA: DataModel{
    internal val inputData : MutableList<DATA> = mutableListOf()
    internal var whereConditions: WhereQuery<IdTable<Long>>? = null
        private set


    fun withInputData(data: List<DATA>) {
        inputData.clear()
        inputData.addAll(data)
    }

    suspend fun <T: IdTable<Long>> withConditions(conditions : WhereQuery<T>) {
        whereConditions = conditions.safeCast<WhereQuery<IdTable<Long>>>().getOrOperationsEx(
            "Cast to <IdTable<Long>> Failed",
            ExceptionCode.CAST_FAILURE
        )
    }

    internal var onStartCallback :  (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onStart(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onStartCallback = callback
    }

    internal var onCompleteCallback : (suspend (sessionId:  RunnableContext)-> Unit)? = null
    suspend fun onComplete(callback: suspend (sessionId:  RunnableContext)-> Unit){
        onCompleteCallback = callback
    }

}

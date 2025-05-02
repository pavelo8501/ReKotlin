package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.exposify.scope.sequence.RunnableContext
import po.exposify.scope.sequence.models.SequenceKey
import kotlin.Long

sealed interface SequenceHandlerInterface {
    val thisKey: SequenceKey
}

class SequenceHandler<DTO, DATA>(
    private  val dtoClassName: String,
    private  val sequenceId: Int,
): SequenceHandlerInterface where DTO : ModelDTO, DATA: DataModel{

    override val thisKey: SequenceKey = SequenceKey(dtoClassName, sequenceId)
    internal val inputData : MutableList<DATA> = mutableListOf()
    internal var whereConditions: WhereCondition<IdTable<Long>>? = null
        private set

    fun withInputData(data: List<DATA>) {
        inputData.clear()
        inputData.addAll(data)
    }

    suspend fun <T: IdTable<Long>> withConditions(conditions : WhereCondition<T>) {
        whereConditions = conditions.safeCast<WhereCondition<IdTable<Long>>>().getOrOperationsEx(
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

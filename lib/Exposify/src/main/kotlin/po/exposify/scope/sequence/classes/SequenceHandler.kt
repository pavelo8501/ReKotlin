package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.models.SequenceKey
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.safeCast
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
        whereConditions = conditions.safeCast<WhereCondition<IdTable<Long>>>().getOrThrowDefault("Cast to <IdTable<Long>> Failed")
    }

}

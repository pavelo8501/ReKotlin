package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.WhereCondition
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.safeCast
import kotlin.Long

sealed interface SequenceHandlerInterface<DTO: ModelDTO> {
    val dtoClass : DTOClass<DTO>
    val thisKey: String

}

class SequenceHandler<DTO, DATA>(
    override val dtoClass: DTOClass<DTO>,
    private  val sequenceId: Int,
): SequenceHandlerInterface<DTO> where DTO : ModelDTO, DATA: DataModel{

    override val thisKey: String = "${dtoClass.personalName}::${sequenceId}"

    internal val inputData : MutableList<DATA> = mutableListOf()

    internal var whereConditions: WhereCondition<IdTable<Long>>? = null
        private set


    fun withInputData(data: List<DATA>) {
        inputData.clear()
        inputData.addAll(data)
    }

    fun <T: IdTable<Long>> withConditions(conditions : WhereCondition<T>) {
        whereConditions = conditions.safeCast<WhereCondition<IdTable<Long>>>().getOrThrowDefault("Cast to <IdTable<Long>> Failed")
    }

}

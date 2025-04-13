package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.QueryConditions
import po.exposify.extensions.WhereCondition
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.ServiceClass


data class SequencePack<DTO, DATA>(
    private val context : SequenceContext<DTO, DATA>,
    internal val serviceClass: ServiceClass<DTO, DATA, ExposifyEntityBase>,
    private val sequenceFn : suspend  SequenceContext<DTO, DATA>.(inputData: List<DATA>, conditions: WhereCondition<IdTable<Long>>?) -> Unit,
    private val handler: SequenceHandler<DTO, DATA>,
) where  DTO : ModelDTO,  DATA : DataModel{


    fun getSequenceHandler():SequenceHandler<DTO, DATA>{
        return handler
    }

    suspend fun start(): List<DATA>{
        context.sequenceFn(handler.inputData, handler.whereConditions)
        return  context.checkout()
    }
}

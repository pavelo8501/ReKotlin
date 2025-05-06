package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.ExecutionProvider
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.misc.collections.CompositeKey


data class SequencePack<DTO,  DATA>(
    val key : CompositeKey<DTOBase<*,*>, SequenceID>,
    private val block : suspend SequenceContext<DTO, DATA, LongEntity>.(SequenceHandler<DTO, DATA>) -> Unit,
) where DTO: ModelDTO,  DATA : DataModel{


    suspend fun start(handler: SequenceHandler<DTO, DATA>){

        val context = SequenceContext<DTO, DATA, LongEntity>(ExecutionProvider(handler.dtoClass))
        block(context, handler)
    }
}

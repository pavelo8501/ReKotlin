package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler

internal data class SwitchData<F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>(
    val childClass: DTOClass<F_DTO, FD, FE>,
    val handlerBlock : suspend ClassSequenceHandler<F_DTO, FD, FE>.()-> Unit,
){
    var sequenceContext :  SequenceContext<F_DTO, FD, FE>? = null
        private set

    fun setSourceContext(context: SequenceContext<F_DTO, FD, FE>){
        sequenceContext = context
    }

}
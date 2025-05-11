package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.ClassSequenceHandler

internal data class SwitchData<F_DTO: ModelDTO, FD: DataModel, FE: LongEntity,  DTO: ModelDTO, D: DataModel, E: LongEntity>(
    val handler : ClassSequenceHandler<DTO, D, E>,
    val query: SwitchQuery<F_DTO, FD, FE>,
    val inputData: D
)
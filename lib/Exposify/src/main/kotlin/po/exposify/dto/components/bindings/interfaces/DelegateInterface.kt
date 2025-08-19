package po.exposify.dto.components.bindings.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KProperty

internal interface DelegateInterface<DTO: ModelDTO, D: DataModel, E: LongEntity>{
    fun updateStatus(status: DelegateStatus)
    var status: DelegateStatus

}
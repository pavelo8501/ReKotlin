package po.exposify.dto.components.bindings.interfaces

import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KProperty

internal interface DelegateInterface<DTO: ModelDTO, F: ModelDTO>{

    fun resolveProperty(property: KProperty<*>)
    fun updateStatus(status: DelegateStatus)

    var status: DelegateStatus
    val hostingClass: DTOBase<DTO, *, *>
}
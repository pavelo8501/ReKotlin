package po.exposify.dto.components.bindings.interfaces

import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.DelegateStatus
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.IdentifiableModuleInstance

interface DelegateInterface<DTO: ModelDTO, F_DTO: ModelDTO> {

    var status: DelegateStatus
    val module: IdentifiableModule
    val hostingClass: DTOBase<DTO, *, *>
    val foreignClass: DTOBase<F_DTO, *, *>


}
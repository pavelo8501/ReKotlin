package po.exposify.common.models

import po.exposify.common.interfaces.OperationResult
import po.exposify.dto.CommonDTO2
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.eventhandler.models.Event





data class CrudResult2<DTO>(
    val rootDTOs: List<CommonDTO2<DTO, *, *>>,
    val event: Event? = null
) where DTO : ModelDTO
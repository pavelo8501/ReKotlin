package po.exposify.common.models

import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.eventhandler.models.Event





data class CrudResult2<DTO>(
    val rootDTOs: List<CommonDTO<DTO, *, *>>,
    val event: Event? = null
) where DTO : ModelDTO
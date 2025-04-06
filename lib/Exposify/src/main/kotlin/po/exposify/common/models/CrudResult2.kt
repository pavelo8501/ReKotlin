package po.exposify.common.models

import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO


data class CrudResult2<DTO>(
    val rootDTOs: List<CommonDTO<DTO, *, *>>,
) where DTO : ModelDTO
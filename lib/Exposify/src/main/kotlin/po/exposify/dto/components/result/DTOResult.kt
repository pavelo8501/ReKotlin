package po.exposify.dto.components.result

import po.exposify.dto.interfaces.ModelDTO

interface DTOResult<DTO: ModelDTO> {
    val dto:DTO
}
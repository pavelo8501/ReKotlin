package po.exposify.dto.models

import po.exposify.dto.interfaces.ModelDTO

@JvmInline
value class DTOId<DTO: ModelDTO>(val id: Long) {

}
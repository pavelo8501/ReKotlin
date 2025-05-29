package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.models.MappingCheck


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> DTOBase<DTO,D,E>.createMappingCheck(from: ValueBased, to : ValueBased):MappingCheck{
   return MappingCheck(this, from, to)
}
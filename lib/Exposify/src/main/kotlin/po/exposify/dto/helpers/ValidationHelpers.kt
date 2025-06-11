package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.data.ColumnMetadata
import po.misc.types.isNotNull

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> responsiveInitialized(
    metadataRecord: ColumnMetadata,
    delegates: List<ResponsiveDelegate<DTO, D, E, *>>
): Boolean{

   val  filtered = delegates.filter { it.name ==  metadataRecord.propertyRecord?.property?.name}
   return filtered.isNotEmpty()
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> parentInitialized(
    metadataRecord: ColumnMetadata,
    delegates: List<ParentDelegate<DTO, D, E, *, *, *>>
): Boolean{

    val filtered = delegates.filter { it.property.name ==  metadataRecord.propertyRecord?.property?.name}

    return filtered.isNotEmpty()
}
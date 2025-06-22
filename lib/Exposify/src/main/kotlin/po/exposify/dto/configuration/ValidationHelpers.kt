package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.data.ColumnMetadata

internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> responsiveInitialized(
    metadataRecord: ColumnMetadata,
    delegates: List<ResponsiveDelegate<DTO, D, E, *>>
): Boolean{
   val  filtered = delegates.filter { it.name ==  metadataRecord.propertyRecord?.property?.name}
   return filtered.isNotEmpty()
}


internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> mandatoryFieldsSetup(
    metadataRecord: ColumnMetadata,
    delegates: List<ResponsiveDelegate<DTO, D, E, *>>,
    attachedForeign: List<AttachedForeignDelegate<DTO, D, E, *, *, *>>
): DelegateInterface<DTO, *>?{
    val foundInResponsive = delegates.firstOrNull { it.name ==  metadataRecord.propertyRecord?.property?.name}
    if(foundInResponsive == null){
        val attached = attachedForeign.firstOrNull{  it.attachedName ==   metadataRecord.propertyRecord?.property?.name}
        return attached
    }else{
        return foundInResponsive
    }
}

internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> parentInitialized(
    metadataRecord: ColumnMetadata,
    delegates: List<ParentDelegate<DTO, D, E, *, *, *>>
): Boolean{
    val filtered = delegates.filter { it.property.name ==  metadataRecord.propertyRecord?.property?.name}
    return filtered.isNotEmpty()
}
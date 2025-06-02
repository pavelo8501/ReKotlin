package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeign
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ComponentType
import po.exposify.dto.models.SourceObject
import po.exposify.extensions.castOrInitEx
import po.misc.types.TypeRecord
import po.misc.validators.models.InstanceRecord
import po.misc.validators.models.ValidationClass
import po.misc.validators.models.ValidationInstance
import po.misc.validators.models.ValidationRecord


inline fun <reified DTO,  reified D, reified E> List<ResponsiveDelegate<DTO, D, E, *>>.toValidator()
: ValidationClass<DTO> where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    val validationObject = ValidationClass(ComponentType.ResponsiveDelegate, TypeRecord.createRecord<DTO>(SourceObject.DTO))
    validationObject.records = map { ValidationRecord(it.propertyRecord) }
    return validationObject
}

@JvmName("toValidatorParentDelegate")
inline fun <DTO, D, E, reified F_DTO> List<ParentDelegate<DTO, D, E, F_DTO, *, *>>.toValidator()
        : ValidationClass<F_DTO> where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO
{
    val parentValidation = ValidationClass(ComponentType.ParentDelegate, TypeRecord.createRecord<F_DTO>(SourceObject.DTO))
    parentValidation.records = map { ValidationRecord(it.propertyRecord) }
    return parentValidation
}

@JvmName("toValidatorAttachedForeignDelegate")
inline fun <DTO, D, E, reified F_DTO, FD, FE> List<AttachedForeign<DTO, D, E, F_DTO, FD, FE>>.toValidator()
: ValidationInstance<DTOBase<F_DTO, FD, FE>>
where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity {
    val attachedValidation = ValidationInstance(
        ComponentType.AttachedDelegate,
        TypeRecord.createRecord<DTOBase<F_DTO, FD, FE>>(ComponentType.DTOClass)
    )
    attachedValidation.records = map { InstanceRecord(it.attachedClass, null) }
    return attachedValidation
}

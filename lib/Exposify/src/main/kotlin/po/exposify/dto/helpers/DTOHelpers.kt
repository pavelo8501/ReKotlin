package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.components.bindings.BindingHub
import po.misc.validators.mapping.models.ValidationClass
import po.misc.validators.mapping.models.ValidationRecord
import kotlin.collections.map


inline fun <reified DTO,  reified D, reified E> BindingHub<DTO, D, E, *, *, *>.createValidation()
        : ValidationClass<DTO> where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    val delegates =  getResponsiveDelegates()
    val validation = ValidationClass<DTO>(hostingDTO)
    validation.validatableRecords = delegates.map { ValidationRecord(it.propertyRecord) }
    return validation
}

fun <DTO, D, E, F_DTO, FD, FE> parentValidation(hostingDTO : CommonDTO<DTO, D,E>)
        : ValidationClass<F_DTO> where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO,  FD: DataModel, FE: LongEntity
{
    val parentValidation = ValidationClass<F_DTO>(hostingDTO)
    hostingDTO.bindingHub.getParentDelegates().forEach {
        parentValidation.addRecord(ValidationRecord(it.propertyRecord))
    }
    return parentValidation
}


//fun <DTO, D, E, F_DTO, FD, FE> createValidation(attachedForeignDelegate : AttachedForeign<DTO, D, E, F_DTO, FD, FE>)
//        : ValidationInstance<DTOBase<F_DTO, FD, FE>> where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO,  FD: DataModel, FE: LongEntity
//{
//    val attachedValidation = ValidationInstance<DTOBase<F_DTO, FD, FE>>(attachedForeignDelegate.hostingDTO)
//    val record = InstanceRecord(attachedForeignDelegate.foreignClass, attachedForeignDelegate.propertyRecord)
//    attachedValidation.addRecord(record)
//    return attachedValidation
//}
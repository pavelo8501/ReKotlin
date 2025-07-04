package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KMutableProperty1

fun <DTO, D, E, F_DTO, FD, FE> CommonDTO<DTO, D, E>.attachedReference(
    foreignDTOClass: RootDTO<F_DTO, FD, FE>,
    dataIdProperty: KMutableProperty1<D, Long>,
    entityIdProperty: KMutableProperty1<E, Long>,
    foreignDTOCallback: (F_DTO)-> Unit
): AttachedForeignDelegate<DTO, D, E, F_DTO, FD, FE>
    where D:DataModel, E: LongEntity, DTO : ModelDTO, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity{

    val container = AttachedForeignDelegate(
        hostingDTO = this,
        foreignClass = foreignDTOClass,
        dataIdProperty = dataIdProperty,
        entityIdProperty =  entityIdProperty,
        foreignDTOCallback = foreignDTOCallback)
    return container
}

fun <DTO, D, E, F_DTO,  FD,  FE> CommonDTO<DTO, D, E>.parentReference(
    foreignDTOClass: DTOBase<F_DTO, FD, FE>,
   parentDTOProvider: D.(F_DTO)-> Unit
): ParentDelegate<DTO, D, E, F_DTO,  FD,  FE>
    where  DTO: ModelDTO, D:DataModel, E : LongEntity, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity {

    val container = ParentDelegate<DTO, D, E, F_DTO,  FD,  FE>(
        hostingDTO = this,
        foreignClass = foreignDTOClass,
        parentDTOProvider = parentDTOProvider)
    return container
}
package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun <DTO,  DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.attachedReference(
    foreignDTOClass: RootDTO<F_DTO, FD, FE>,
    dataIdProperty: KMutableProperty1<DATA, Long>,
    entityIdProperty: KMutableProperty1<ENTITY,  Long>,
    foreignDTOCallback: (F_DTO)-> Unit
): AttachedForeignDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD: DataModel,  FE: LongEntity
{

    val container = AttachedForeignDelegate(this, foreignDTOClass, dataIdProperty, entityIdProperty, foreignDTOCallback)
    return container
}

fun <DTO, DATA,  ENTITY, F_DTO,  FD,  FE> CommonDTO<DTO, DATA, ENTITY>.parentReference(
    foreignDTOClass: DTOBase<F_DTO, FD, FE>,
   parentDTOProvider: DATA.(F_DTO)-> Unit
): ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>
        where  DTO: ModelDTO, DATA:DataModel, ENTITY : LongEntity,
               F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    val container = ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>(this, foreignDTOClass, parentDTOProvider)
    return container
}
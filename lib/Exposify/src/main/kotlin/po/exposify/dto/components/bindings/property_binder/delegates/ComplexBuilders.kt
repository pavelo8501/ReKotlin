package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KProperty1

fun <DTO,  DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.attachedReference(
    foreignDTOClass:  DTOBase<F_DTO, FD, FE>,
    dataIdProperty: KProperty1<DATA, Long>,
    foreignDTOProvider: DATA.(F_DTO)-> Unit
): AttachedForeignDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO,
              F_DTO: ModelDTO, FD: DataModel,  FE: LongEntity
{
    val container = AttachedForeignDelegate(this, foreignDTOClass, dataIdProperty, foreignDTOProvider)
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
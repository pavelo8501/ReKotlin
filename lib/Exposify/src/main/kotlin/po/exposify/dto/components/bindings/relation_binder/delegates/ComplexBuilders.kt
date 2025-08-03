package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KMutableProperty1

fun <DTO, D, E, F, FD, FE> CommonDTO<DTO, D, E>.attachedReference(
    foreignDTOClass: RootDTO<F, FD, FE>,
    dataIdProperty: KMutableProperty1<D, Long>,
    entityIdProperty: KMutableProperty1<E, Long>,
    foreignDTOCallback: (F) -> Unit,
): AttachedForeignDelegate<DTO, D, E, F, FD, FE>
    where D : DataModel, E : LongEntity, DTO : ModelDTO, F : ModelDTO, FD : DataModel, FE : LongEntity {
    val container =
        AttachedForeignDelegate(
            hostingDTO = this,
            foreignClass = foreignDTOClass,
            dataIdProperty = dataIdProperty,
            entityIdProperty = entityIdProperty,
            foreignDTOCallback = foreignDTOCallback,
        )
    return container
}

fun <DTO, D, E, F, FD, FE> CommonDTO<DTO, D, E>.parentReference(
    foreignDTOClass: DTOBase<F, FD, FE>,
    entityProperty: KMutableProperty1<E, FE>,
    parentDTOProvider: D.(F) -> Unit,
): ParentDelegate<DTO, D, E, F, FD, FE>
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    val container =
        ParentDelegate<DTO, D, E, F, FD, FE>(
            hostingDTO = this,
            foreignClass = foreignDTOClass,
            entityProperty,
            parentDTOProvider = parentDTOProvider,
        )
    return container
}

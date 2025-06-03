package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ModelDTO
import po.misc.validators.models.MappingCheck
import kotlin.reflect.KMutableProperty1


inline fun <reified DTO,  DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.attachedReference(
    foreignDTOClass:  DTOBase<F_DTO, FD, FE>,
    noinline  foreignDTOProvider: DATA.(F_DTO)-> Unit
): AttachedForeign<DTO, DATA, ENTITY, F_DTO, FD, FE>
    where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO,
          F_DTO: ModelDTO, FD: DataModel,  FE: LongEntity
{
    val container = AttachedForeign(this, foreignDTOClass, foreignDTOProvider)
    return container
}

inline fun <reified DTO, DATA,  ENTITY, F_DTO,  FD,  FE>  CommonDTO<DTO, DATA, ENTITY>.parentReference(
    foreignDTOClas: DTOBase<F_DTO, FD, FE>,
   noinline foreignDTOProvider: (DATA.(F_DTO)-> Unit)? = null
): ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>
        where  DTO: ModelDTO, DATA:DataModel, ENTITY : LongEntity,
               F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    val container = ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>(this, foreignDTOClas, foreignDTOProvider)
    return container
}

fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, dataProperty, entityProperty)
    if(tracker.config.observeProperties){
        propertyDelegate.subscribeUpdates(tracker, tracker::propertyUpdated)
    }
    return propertyDelegate
}


fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate(this, dataProperty, null)
    if(tracker.config.observeProperties){
        propertyDelegate.subscribeUpdates(tracker, tracker::propertyUpdated)
    }
    return propertyDelegate
}

inline fun <reified DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.binding(
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, null, null)
    if(tracker.config.observeProperties){
        propertyDelegate.subscribeUpdates(tracker, tracker::propertyUpdated)
    }
    bindingHub.setBinding(propertyDelegate)
    return propertyDelegate
}



fun <DTO, D, E, V: Any>  CommonDTO<DTO, D, E>.serializedBinding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
): SerializedDelegate<DTO, D, E, V>
    where DTO: ModelDTO, D: DataModel, E: LongEntity{

    val delegate = SerializedDelegate(this, dataProperty, entityProperty, emptyList())
    bindingHub.setBinding(delegate)
    return delegate
}

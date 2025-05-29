package po.exposify.dto.components.bindings.property_binder.delegates

import org.jetbrains.exposed.dao.LongEntity

import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.ModelDTO
import po.misc.reflection.properties.models.MappingCheck
import kotlin.reflect.KMutableProperty1



fun <DTO,  DATA, ENTITY, F_DTO, FD, FE> CommonDTO<DTO, DATA, ENTITY>.foreign2IdReference(
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty: KMutableProperty1<ENTITY, FE>,
    foreignDTOClass:  DTOBase<F_DTO, FD, FE>,
): ForeignIDClassDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
    where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO,
          F_DTO: ModelDTO, FD: DataModel,  FE: LongEntity
{
    val container = ForeignIDClassDelegate(this, dataProperty, entityProperty, foreignDTOClass)
    bindingHub.setBinding(container)
    return container
}

/**
 * Creates a reference to a parent entity **within the current DTO hierarchy** using its ID.
 *
 * Use this when the parent is managed as another DTO and should be resolved via internal binding,
 * without requiring you to provide an entity class.
 *
 * Example: `val pageId by parent2IdReference(TestSection::pageId, TestSectionEntity::page)`
 *
 * @param dataProperty The property in your data model that holds the parent entity's ID.
 * @param entityProperty The DAO property referencing the parent entity.
 */

fun <DTO, DATA, ENTITY, FE> CommonDTO<DTO, DATA, ENTITY>.parent2IdReference(
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty: KMutableProperty1<ENTITY, FE>
): ParentIDDelegate<DTO, DATA, ENTITY, FE>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, FE: LongEntity
{
    val container = ParentIDDelegate(this, dataProperty, entityProperty)
    bindingHub.setBinding(container)
    return container
}

/**
 * Creates a reference to a full parent DTO object from the current DTO.
 *
 * Use this when you want to access the parent DTO’s properties (e.g., `page.title`) directly
 * or when the nested structure should remain active and consistent.
 *
 * Example: `val page by parentReference(TestSection::page, TestPageDTO, TestPageEntity)`
 *
 * @param dataProperty The property in your data model that holds the parent’s data model.
 * @param parentDtoClass The parent DTO class reference (e.g., `TestPageDTO`).
 * @param parentEntityModel The DAO class of the parent entity (e.g., `TestPageEntity`).
 */

fun <DTO, DATA,  ENTITY, F_DTO,  FD,  FE>  CommonDTO<DTO, DATA, ENTITY>.parentReference(
    dataProperty : KMutableProperty1<DATA, FD>,
    parentDtoClass: DTOClass<F_DTO, FD, FE>,
   foreignDTOClas: DTOClass<F_DTO, FD, FE>
): ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>
        where  DTO: ModelDTO, DATA:DataModel, ENTITY : LongEntity,
               F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    val container = ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>(this, dataProperty, parentDtoClass, foreignDTOClas)
    bindingHub.setBinding(container)
    return container
}

inline fun <DTO, D, E, reified V: Any>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty :KMutableProperty1<E, V>
): PropertyDelegate<DTO, D, E, V>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate<DTO, D, E, V>(this, dataProperty, entityProperty)
    if(tracker.config.observeProperties){
        propertyDelegate.subscribeUpdates(tracker,  tracker::propertyUpdated)
    }
    return propertyDelegate
}


inline fun <DTO, D, E, reified V: Any>  CommonDTO<DTO, D, E>.binding(
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

inline fun <DTO, D, E, reified V: Any>  CommonDTO<DTO, D, E>.binding(
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

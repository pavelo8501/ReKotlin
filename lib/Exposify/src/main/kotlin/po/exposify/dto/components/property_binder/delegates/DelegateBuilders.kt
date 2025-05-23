package po.exposify.dto.components.property_binder.delegates

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.dao.classes.JSONBType
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SerializedBinding
import po.exposify.dto.interfaces.ModelDTO
import po.misc.types.getKType
import kotlin.reflect.KMutableProperty1


/**
 * Creates a reference to a foreign entity **outside the current DTO hierarchy** using its ID.
 *
 * Use this when referencing static or external entities (e.g., users, roles, logs)
 * that aren't managed as nested DTOs but are stored as foreign keys.
 *
 * Example: `val updatedBy by foreign2IdReference(TestSection::updatedBy, TestSectionEntity::updatedBy, TestUserEntity)`
 *
 * @param dataProperty The property in your data model that holds the foreign entity's ID.
 * @param entityProperty The DAO property referencing the foreign entity.
 * @param foreignEntityModel The DAO class (e.g., `TestUserEntity`) used to look up the foreign entity by ID.
 */

fun <DTO,  DATA, ENTITY, FE> CommonDTO<DTO, DATA, ENTITY>.foreign2IdReference(
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty: KMutableProperty1<ENTITY, FE>,
    foreignEntityModel: LongEntityClass<FE>,
): ForeignIDClassDelegate<DTO, DATA, ENTITY, FE>
    where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, FE: LongEntity
{
    val container = ForeignIDClassDelegate(this, dataProperty, entityProperty, foreignEntityModel)
    dtoPropertyBinder.setBinding(container)
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
    dtoPropertyBinder.setBinding(container)
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
    parentEntityModel: LongEntityClass<FE>
): ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>
        where  DTO: ModelDTO, DATA:DataModel, ENTITY : LongEntity,
               F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
{
    val container = ParentDelegate<DTO, DATA, ENTITY, F_DTO,  FD,  FE>(this, dataProperty, parentDtoClass, parentEntityModel)
    dtoPropertyBinder.setBinding(container)
    return container
}

inline fun <DTO, D, E, reified R>  CommonDTO<DTO, D, E>.binding(
    dataProperty:KMutableProperty1<D, R>,
    entityProperty :KMutableProperty1<E, R>
): PropertyDelegate<DTO, D, E, R>
        where  DTO: ModelDTO, D:DataModel, E : LongEntity
{
    val propertyDelegate = PropertyDelegate(this, dataProperty, entityProperty)
    dtoPropertyBinder.setBinding(propertyDelegate)
    return propertyDelegate
}

fun <DTO, D, E, S, V>  CommonDTO<DTO, D, E>.serializedBinding(
    dataProperty:KMutableProperty1<D, V>,
    entityProperty:KMutableProperty1<E, V>,
    serializableClass:  JSONBType<S>
): SerializedDelegate<DTO, D, E, List<S>, V>
    where DTO: ModelDTO, D: DataModel, E: LongEntity, S: Any
{
    val serializedDelegate = SerializedDelegate(this, dataProperty, entityProperty, serializableClass.listSerializer)
    dtoPropertyBinder.setBinding(serializedDelegate)
    dtoFactory.setSerializableType(dataProperty.name, serializableClass.serializer)
    return serializedDelegate
}


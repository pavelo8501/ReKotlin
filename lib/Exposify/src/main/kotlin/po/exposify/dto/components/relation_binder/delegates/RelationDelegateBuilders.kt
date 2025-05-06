package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.dto.components.relation_binder.createOneToManyContainer
import po.exposify.dto.components.relation_binder.createOneToOneContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.lognotify.extensions.newTaskAsync
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


/**
 * Binds a one-to-one relationship between this [CommonDTO] and a child DTO.
 *
 * This delegate sets up the necessary linkage, container binding, and repository initialization
 * to handle a 1:1 relationship. It automatically ensures the child DTO class is initialized
 * and attaches it to the relation binder for lifecycle management.
 *
 * @param childClass The [DTOClass] of the child DTO participating in the 1:1 relationship.
 * @param ownDataModel The property in the parent [DATA] model that holds the child [CD] data.
 * @param ownEntity The property in the parent [ENTITY] referencing the child [CF] entity.
 * @param foreignEntity The property in the child [CF] entity that references the parent [ENTITY].
 *
 * @return A [OneToOneDelegate] that can be used as a delegated property in the DTO class.
 */
fun <DTO, DATA, ENTITY, C_DTO, CD,  CF> CommonDTO<DTO, DATA, ENTITY>.oneToOnOf(
    childClass: DTOBase<C_DTO, CD>,
    ownDataModel: KMutableProperty1<DATA, out CD?>,
    ownEntity: KProperty1<ENTITY, CF>,
    foreignEntity: KMutableProperty1<CF, ENTITY>
): OneToOneDelegate<DTO, DATA, ENTITY, C_DTO, CD,  CF>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, C_DTO: ModelDTO,  CD: DataModel, CF: LongEntity
{
    val result =  newTaskAsync("Hierarchy Initialization. Child dto ${childClass.personalName}") {

        val castedOwnDataModel = ownDataModel.castOrInitEx<KMutableProperty1<DATA, CD>>()
        val bindingDelegate =  OneToOneDelegate(this,childClass, castedOwnDataModel, ownEntity, foreignEntity)
        val container =  childClass.createOneToOneContainer(this, bindingDelegate)
        val repository = createRepository(container)
        val binder  =  dtoClassConfig.relationBinder.castOrOperationsEx<RelationshipBinder<DTO, DATA, ENTITY, C_DTO, CD, CF>>()
        binder.attachBinding(Cardinality.ONE_TO_ONE, container)
        bindingDelegate

    }.resultOrException()

    return result
}

/**
 * Binds a one-to-many relationship between this [CommonDTO] and a child DTO.
 *
 * This delegate initializes and registers a container and repository to manage a collection
 * of child DTOs related to this parent. It automatically triggers child DTO setup and binds
 * it through the [RelationshipBinder].
 *
 * @param childClass The [DTOClass] of the child DTOs in the 1:N relationship.
 * @param ownDataModels The property in the parent [DATA] model that holds the child [CD] list.
 * @param foreignEntities The property in the parent [ENTITY] referencing the child [CF] entities.
 *
 * @return A [OneToManyDelegate] that exposes the bound child DTOs as a read-only list.
 */
fun <DTO, DATA, ENTITY, C_DTO, CD,  CF> CommonDTO<DTO, DATA, ENTITY>.oneToManyOf(
    childClass: DTOBase<C_DTO, CD>,
    ownDataModels: KProperty1<DATA, MutableList<out CD>>,
    ownEntities: KProperty1<ENTITY, SizedIterable<CF>>,
    foreignEntity: KMutableProperty1<CF, ENTITY>
): OneToManyDelegate<DTO, DATA, ENTITY, C_DTO, CD, CF>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, C_DTO: ModelDTO,  CD: DataModel, CF: LongEntity
{
    return newTaskAsync("Hierarchy Initialization. Child dto ${childClass.personalName}") {

        val castedOwnDataModels = ownDataModels.castOrInitEx<KProperty1<DATA, MutableList<CD>>>()
        val bindingDelegate = OneToManyDelegate(this,childClass,  castedOwnDataModels, ownEntities, foreignEntity)
        val container =  childClass.createOneToManyContainer(this, bindingDelegate)
        val repository = createRepository(container)
        val binder = dtoClassConfig.relationBinder.castOrOperationsEx<RelationshipBinder<DTO, DATA, ENTITY, C_DTO, CD, CF>>()
        binder.attachBinding(Cardinality.ONE_TO_MANY, container)
        bindingDelegate

   }.resultOrException()
}
package po.exposify.dto.components.relation_binder

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOBase
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.property_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.dto.components.relation_binder.classes.NullablePropertyWrapper
import po.exposify.dto.components.relation_binder.delegates.OneToManyDelegate
import po.exposify.dto.components.relation_binder.delegates.OneToOneDelegate
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.extensions.castOrOperationsEx
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


fun <DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY> DTOBase<CHILD_DTO, CHILD_DATA>.createOneToOneContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    bindingDelegate : OneToOneDelegate<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
): SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>

where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
       CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
   return  SingleChildContainer(dto, this, bindingDelegate)
}


fun <DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY> DTOBase<CHILD_DTO, CHILD_DATA>.createOneToManyContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    bindingDelegate : OneToManyDelegate<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
): MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    return  MultipleChildContainer(dto, this, bindingDelegate)
}



class SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>  (
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOBase<CHILD_DTO, CHILD_DATA>,
    private val bindingDelegate : OneToOneDelegate<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
            CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    override val thisKey = BindingKeyBase.createOneToOneKey<DTO>(dto.dtoClass)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    fun getDataModel(dataModel: DATA): CHILD_DATA
            = bindingDelegate.getDataModel(dataModel)

    fun setForeignEntity(container: EntityUpdateContainer<ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>): Unit
            = bindingDelegate.setForeignEntity(container)

    fun getChildEntity(entity: ENTITY): CHILD_ENTITY
            = bindingDelegate.getChildEntity(entity)

}


class MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOBase<CHILD_DTO, CHILD_DATA>,
    private val bindingDelegate : OneToManyDelegate<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    val delegateName get() = bindingDelegate.qualifiedName

    override val thisKey  = BindingKeyBase.createOneToManyKey(dto.dtoClass)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY


    fun getDataModels(dataModel: DATA): List<CHILD_DATA>
        = bindingDelegate.getDataModels(dataModel)

    fun setForeignEntity(container: EntityUpdateContainer<ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>): Unit
        = bindingDelegate.setForeignEntity(container)

    fun getChildEntities(entity: ENTITY): List<CHILD_ENTITY>
        = bindingDelegate.getChildEntities(entity)

}


sealed class BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    val childClass: DTOBase<CHILD_DTO, CHILD_DATA>,
) where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity, CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    val  childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
        get() = childClass.config.castOrOperationsEx<DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()

    abstract val cardinality  : Cardinality
    abstract val thisKey : BindingKeyBase

}
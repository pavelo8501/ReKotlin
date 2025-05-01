package po.exposify.classes.components

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.classes.DTOBase
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.ClassDTO
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntity


interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntity>{

    suspend fun withRelationshipBinderAsync(block: suspend (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>)-> Unit)
    fun withRelationshipBinder(block:  (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>)-> Unit)
}

class DTOConfig<DTO, DATA, ENTITY>(
    val registry : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    val dtoClass : DTOBase<DTO, *>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO,  ENTITY : ExposifyEntity, DATA: DataModel{

   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(registry.commonDTOKClass, registry.dataKClass, this)

    var binderPropertyUpdate  : ((String, PropertyType, UpdateMode) -> Unit)? = null

    val namedSerializes = mutableListOf<Pair<String, KSerializer<out Any>>>()
    val propertyBinder : PropertyBinder<DATA, ENTITY> = PropertyBinder(binderPropertyUpdate   ){syncedSerializedList->
        syncedSerializedList.forEach {
            namedSerializes.add(it.getSerializer())
        }
        dtoFactory.setSerializableTypes(namedSerializes)
    }

    val relationBinder: RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>
            = RelationshipBinder(dtoClass)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    override suspend fun withRelationshipBinderAsync(block: suspend (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>)-> Unit){
        block.invoke(relationBinder)
    }
    override fun withRelationshipBinder(block: (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>)-> Unit){
        block.invoke(relationBinder)
    }

    fun propertyBindings(vararg props: PropertyBindingOption<DATA, ENTITY, *> ): Unit =  propertyBinder.setProperties(props.toList())

    inline fun childBindings(
        block: RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, ExposifyEntity>.()-> Unit){
        relationBinder.block()
    }

    suspend fun hierarchyMembers(vararg childDTO : DTOClass<*>){
        childDTO.toList().forEach {
            relationBinder.addChildClass(it)
        }
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}
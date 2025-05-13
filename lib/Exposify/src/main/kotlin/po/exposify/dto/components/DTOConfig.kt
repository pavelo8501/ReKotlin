package po.exposify.dto.components

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass


interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity>{

    suspend fun withRelationshipBinderAsync(block: suspend (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>)-> Unit)
    fun withRelationshipBinder(block:  (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>)-> Unit)
}

class DTOConfig<DTO, DATA, ENTITY>(
    val registry : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel: ExposifyEntityClass<ENTITY>,
    val dtoClass : DTOBase<DTO, DATA , ENTITY>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity{

   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(registry.commonDTOKClass, registry.dataKClass, this)
   internal var daoService :  DAOService<DTO, DATA, ENTITY> =  DAOService(dtoClass, registry)

   // var binderPropertyUpdate  : ((String, PropertyType, UpdateMode) -> Unit)? = null

   // val namedSerializes = mutableListOf<Pair<String, KSerializer<out Any>>>()
//    val propertyBinder : PropertyBinder<DATA, ENTITY> = PropertyBinder(binderPropertyUpdate   ){syncedSerializedList->
//        syncedSerializedList.forEach {
//            namedSerializes.add(it.getSerializer())
//        }
//        dtoFactory.setSerializableTypes(namedSerializes)
//    }

    val relationBinder: RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>
            = RelationshipBinder(dtoClass)

    override suspend fun withRelationshipBinderAsync(block: suspend (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>)-> Unit){
        block.invoke(relationBinder)
    }
    override fun withRelationshipBinder(block: (RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>)-> Unit){
        block.invoke(relationBinder)
    }

   // fun propertyBindings(vararg props: PropertyBindingOption<DATA, ENTITY, *> ): Unit =  propertyBinder.setProperties(props.toList())

    inline fun childBindings(
        block: RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>.()-> Unit){
        relationBinder.block()
    }

    suspend fun hierarchyMembers(vararg childDTO : DTOClass<*, *, *>){
        childDTO.toList().forEach {
            relationBinder.addChildClass(it)
        }
    }

    fun useDataModelBuilder(builderFn: () -> DATA)
        = dtoFactory.setDataModelConstructor(builderFn)

}
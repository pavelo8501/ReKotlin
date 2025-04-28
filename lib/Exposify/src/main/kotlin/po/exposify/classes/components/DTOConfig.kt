package po.exposify.classes.components

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase


interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase>{

    fun initFactoryRoutines()
    suspend fun withRelationshipBinder(block: suspend (RelationshipBinder<DTO, DATA, ENTITY>)-> Unit)
}


class DTOConfig<DTO, DATA, ENTITY>(
    val registry : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass<DTO>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO,  ENTITY : ExposifyEntityBase, DATA: DataModel{


   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(registry.commonDTOKClass, registry.dataKClass, this)

    var binderPropertyUpdate  : (suspend (String, PropertyType, UpdateMode) -> Unit)? = null

    val namedSerializes = mutableListOf<Pair<String, KSerializer<out Any>>>()
    val propertyBinder : PropertyBinder<DATA, ENTITY> = PropertyBinder(binderPropertyUpdate   ){syncedSerializedList->
        syncedSerializedList.forEach {
            namedSerializes.add(it.getSerializer())
        }
        dtoFactory.setSerializableTypes(namedSerializes)
    }

    val relationBinder: RelationshipBinder<DTO, DATA, ENTITY> = RelationshipBinder<DTO, DATA, ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set


    override suspend fun withRelationshipBinder(block: suspend (RelationshipBinder<DTO, DATA, ENTITY>)-> Unit){
        block.invoke(relationBinder)
    }
    override fun initFactoryRoutines(){
        val factory = dtoFactory
        factory.setPostCreationRoutine("dto_initialization") {
            val dataModelContainer = DataModelContainer<DTO, DATA>(dataModel, factory.dataBlueprint)
            initialize(registry, dataModelContainer, this@DTOConfig.propertyBinder)
        }
        factory.setPostCreationRoutine("repository_bindings") {
            relationBinder.createRepositories(this)
        }
    }

    fun propertyBindings(vararg props: PropertyBindingOption<DATA, ENTITY, *> ): Unit =  propertyBinder.setProperties(props.toList())

    inline fun childBindings(
        block: RelationshipBinder<DTO, DATA, ENTITY>.()-> Unit){
        relationBinder.block()
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}
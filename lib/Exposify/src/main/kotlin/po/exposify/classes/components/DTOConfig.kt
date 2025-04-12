package po.exposify.classes.components

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.bindings.SyncedSerialized
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase


internal interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase>{

    fun initFactoryRoutines()
    suspend  fun withFactory(block: suspend (DTOFactory<DTO, DATA, ENTITY>)-> Unit)
    suspend fun withRelationshipBinder(block: suspend (RelationshipBinder<DTO, DATA, ENTITY>)-> Unit)
}

internal class DTOConfig<DTO, DATA, ENTITY>(
    val dtoRegItem : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass<DTO>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO,  ENTITY : ExposifyEntityBase, DATA: DataModel{

    val dtoFactory: DTOFactory<DTO, DATA, ENTITY> =
        DTOFactory<DTO, DATA, ENTITY>(dtoRegItem.commonDTOKClass, dtoRegItem.dataKClass, this)

    val propertyBinder : PropertyBinder<DATA, ENTITY> = PropertyBinder(){syncedSerializedList->
        val namedSerializes = mutableListOf<Pair<String, KSerializer<out Any>>>()
        syncedSerializedList.forEach {
            namedSerializes.add(it.getSerializer())
        }
        dtoFactory.setSerializableTypes(namedSerializes.toList())
    }

    val relationBinder: RelationshipBinder<DTO, DATA, ENTITY> = RelationshipBinder<DTO, DATA, ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    override  suspend fun withFactory(block: suspend (DTOFactory<DTO, DATA, ENTITY>)-> Unit){
        block.invoke(dtoFactory)
    }

    override suspend fun withRelationshipBinder(block: suspend (RelationshipBinder<DTO, DATA, ENTITY>)-> Unit){
        block.invoke(relationBinder)
    }
    override fun initFactoryRoutines(){
        dtoFactory.setPostCreationRoutine("dto_initialization") {
            val dataModelContainer = DataModelContainer<DTO, DATA>(dataModel, this@DTOConfig.dtoFactory.dataBlueprint)
            val thisRegItem = this@DTOConfig.dtoRegItem
            val thisPropertyBinder = this@DTOConfig.propertyBinder
            initialize(thisRegItem, dataModelContainer, thisPropertyBinder)
        }
        dtoFactory.setPostCreationRoutine("repository_bindings") {
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
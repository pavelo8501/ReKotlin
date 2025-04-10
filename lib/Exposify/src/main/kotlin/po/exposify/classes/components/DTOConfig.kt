package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.binders.PropertyBinder
import po.exposify.binders.PropertyBindingOption
import po.exposify.binders.UpdateMode
import po.exposify.binders.relationship.RelationshipBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.safeCast


internal interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase>{

    fun initFactoryRoutines()
    suspend  fun withFactory(block: suspend (DTOFactory<DTO, DATA, ENTITY>)-> Unit)
    suspend fun withRelationshipBinder(block: suspend (RelationshipBinder<DTO, DATA, ENTITY>)-> Unit)
   // suspend fun withDaoService(block: suspend (DAOService<DTO, DATA, ENTITY>)-> Unit)
}

internal class DTOConfig<DTO, DATA, ENTITY>(
    val dtoRegItem : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass<DTO>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO,  ENTITY : ExposifyEntityBase, DATA: DataModel{

    val dtoFactory: DTOFactory<DTO, DATA, ENTITY> =
        DTOFactory<DTO, DATA, ENTITY>(dtoRegItem.commonDTOKClass, dtoRegItem.dataKClass, this)

//    val daoService: DAOService<DTO, DATA, ENTITY> = DAOService<DTO, DATA, ENTITY>(false, entityModel)
    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()

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